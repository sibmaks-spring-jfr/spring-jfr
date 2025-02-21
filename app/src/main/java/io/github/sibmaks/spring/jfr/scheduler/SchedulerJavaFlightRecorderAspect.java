package io.github.sibmaks.spring.jfr.scheduler;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.scheduled.ScheduledMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.scheduled.ScheduledMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.scheduled.ScheduledMethodFailedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Scheduled;

@Aspect
public class SchedulerJavaFlightRecorderAspect {
    private final String contextId;

    public SchedulerJavaFlightRecorderAspect(ContextIdProvider contextIdProvider) {
        this.contextId = contextIdProvider.getContextId();
    }

    @Pointcut("@annotation(scheduled)")
    public void scheduledMethods(Scheduled scheduled) {
    }

    @Around(value = "scheduledMethods(scheduled)", argNames = "joinPoint,scheduled")
    public Object traceScheduledMethods(ProceedingJoinPoint joinPoint, Scheduled scheduled) throws Throwable {
        var invocationId = InvocationContext.startTrace();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        ScheduledMethodCalledEvent.builder()
                .contextId(contextId)
                .invocationId(invocationId)
                .className(methodSignature.getDeclaringType().getCanonicalName())
                .methodName(methodSignature.getName())
                .build()
                .commit();

        try {
            var args = joinPoint.getArgs();
            var result = joinPoint.proceed(args);

            ScheduledMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build()
                    .commit();

            return result;
        } catch (Throwable throwable) {
            ScheduledMethodFailedEvent.builder()
                    .invocationId(invocationId)
                    .exceptionClass(throwable.getClass().getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build()
                    .commit();

            throw throwable;
        } finally {
            InvocationContext.stopTrace(invocationId);
        }
    }
}
