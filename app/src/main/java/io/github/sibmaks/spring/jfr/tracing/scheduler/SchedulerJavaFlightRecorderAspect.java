package io.github.sibmaks.spring.jfr.tracing.scheduler;

import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.tracing.scheduled.ScheduledMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.scheduled.ScheduledMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.scheduled.ScheduledMethodFailedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class SchedulerJavaFlightRecorderAspect {
    private final String contextId;

    public SchedulerJavaFlightRecorderAspect(String contextId) {
        this.contextId = contextId;
    }

    @Pointcut("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void scheduledMethods() {
    }

    @Around(value = "scheduledMethods()", argNames = "joinPoint")
    public Object traceScheduledMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        var invocationId = InvocationContext.startTrace();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        var declaringType = methodSignature.getDeclaringType();
        ScheduledMethodCalledEvent.builder()
                .contextId(contextId)
                .invocationId(invocationId)
                .className(declaringType.getCanonicalName())
                .methodName(methodSignature.getName())
                .build()
                .commit();

        try {
            var result = joinPoint.proceed();

            ScheduledMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build()
                    .commit();

            return result;
        } catch (Throwable throwable) {
            var throwableClass = throwable.getClass();
            ScheduledMethodFailedEvent.builder()
                    .invocationId(invocationId)
                    .exceptionClass(throwableClass.getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build()
                    .commit();

            throw throwable;
        } finally {
            InvocationContext.stopTrace(invocationId);
        }
    }
}
