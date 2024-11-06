package io.github.sibmaks.spring.jfr.scheduler;

import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.scheduled.ScheduledMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.scheduled.ScheduledMethodFailedEvent;
import io.github.sibmaks.spring.jfr.event.scheduled.ScheduledMethodInvokedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Scheduled;

@Aspect
public class SchedulerJavaFlightRecorderAspect {

    @Pointcut("@annotation(scheduled)")
    public void scheduledMethods(Scheduled scheduled) {
    }

    @Around(value = "scheduledMethods(scheduled)", argNames = "joinPoint,scheduled")
    public Object traceScheduledMethods(ProceedingJoinPoint joinPoint, Scheduled scheduled) throws Throwable {
        var invocationId = InvocationContext.startTrace();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        var event = ScheduledMethodInvokedEvent.builder()
                .invocationId(invocationId)
                .className(methodSignature.getDeclaringType().getCanonicalName())
                .methodName(methodSignature.getName())
                .build();
        event.commit();

        try {
            var args = joinPoint.getArgs();
            var result = joinPoint.proceed(args);

            var finishedEvent = ScheduledMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build();
            finishedEvent.commit();

            return result;
        } catch (Throwable throwable) {
            var failEvent = ScheduledMethodFailedEvent.builder()
                    .invocationId(invocationId)
                    .exceptionClass(throwable.getClass().getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build();
            failEvent.commit();

            throw throwable;
        } finally {
            InvocationContext.stopTrace(invocationId);
        }
    }
}
