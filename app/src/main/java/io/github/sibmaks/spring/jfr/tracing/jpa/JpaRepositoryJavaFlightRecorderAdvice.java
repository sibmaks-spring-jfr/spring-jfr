package io.github.sibmaks.spring.jfr.tracing.jpa;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.tracing.jpa.JPAMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.jpa.JPAMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.jpa.JPAMethodFailedEvent;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class JpaRepositoryJavaFlightRecorderAdvice implements MethodInterceptor {
    private final String className;
    private final String contextId;
    private final JavaFlightRecorderRecordCounter flightRecorderRecordCounter;

    public JpaRepositoryJavaFlightRecorderAdvice(
            String className,
            String contextId,
            JavaFlightRecorderRecordCounter flightRecorderRecordCounter
    ) {
        this.className = className;
        this.contextId = contextId;
        this.flightRecorderRecordCounter = flightRecorderRecordCounter;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (!flightRecorderRecordCounter.hasRunningRecording()) {
            return methodInvocation.proceed();
        }
        var correlationId = InvocationContext.getTraceId();
        var invocationId = InvocationContext.startTrace();
        try {
            var method = methodInvocation.getMethod();

            JPAMethodCalledEvent.builder()
                    .contextId(contextId)
                    .correlationId(correlationId)
                    .invocationId(invocationId)
                    .className(className)
                    .methodName(method.getName())
                    .build()
                    .commit();

            try {
                var result = methodInvocation.proceed();

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
            }
        } finally {
            InvocationContext.stopTrace(invocationId);
        }
    }

}
