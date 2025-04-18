package io.github.sibmaks.spring.jfr.tracing.jpa;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.tracing.jpa.JPAMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.jpa.JPAMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.jpa.JPAMethodFailedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class JpaRepositoryJavaFlightRecorderAspect {
    private final String contextId;
    private final JavaFlightRecorderRecordCounter flightRecorderRecordCounter;

    public JpaRepositoryJavaFlightRecorderAspect(
            String contextId,
            JavaFlightRecorderRecordCounter flightRecorderRecordCounter
    ) {
        this.contextId = contextId;
        this.flightRecorderRecordCounter = flightRecorderRecordCounter;
    }

    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void jpaRepositoryMethods() {
    }

    @Around("jpaRepositoryMethods()")
    public Object traceJpaRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!flightRecorderRecordCounter.hasRunningRecording()) {
            return joinPoint.proceed();
        }
        var correlationId = InvocationContext.getTraceId();
        var invocationId = InvocationContext.startTrace();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        var declaringType = methodSignature.getDeclaringType();
        JPAMethodCalledEvent.builder()
                .contextId(contextId)
                .correlationId(correlationId)
                .invocationId(invocationId)
                .className(declaringType.getCanonicalName())
                .methodName(methodSignature.getName())
                .build()
                .commit();

        try {
            var result = joinPoint.proceed();

            JPAMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build()
                    .commit();

            return result;
        } catch (Throwable throwable) {
            var throwableClass = throwable.getClass();
            JPAMethodFailedEvent.builder()
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
