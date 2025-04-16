package io.github.sibmaks.spring.jfr.tracing.service;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.tracing.service.ServiceMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.service.ServiceMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.service.ServiceMethodFailedEvent;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

/**
 * Spring Java Flight recorder {@link Service} invocation aspect.
 *
 * @author sibmaks
 * @since 0.0.10
 */
@Aspect
public class ServiceJavaFlightRecorderAspect implements Advice, MethodInterceptor {
    private final String className;
    private final String contextId;
    private final JavaFlightRecorderRecordCounter flightRecorderRecordCounter;

    public ServiceJavaFlightRecorderAspect(
            String className,
            String contextId,
            JavaFlightRecorderRecordCounter flightRecorderRecordCounter
    ) {
        this.className = className;
        this.contextId = contextId;
        this.flightRecorderRecordCounter = flightRecorderRecordCounter;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (!flightRecorderRecordCounter.hasRunningRecording()) {
            return invocation.proceed();
        }
        var correlationId = InvocationContext.getTraceId();
        var invocationId = InvocationContext.startTrace();
        try {
            var method = invocation.getMethod();

            ServiceMethodCalledEvent.builder()
                    .correlationId(correlationId)
                    .contextId(contextId)
                    .invocationId(invocationId)
                    .className(className)
                    .methodName(method.getName())
                    .build()
                    .commit();

            try {
                var result = invocation.proceed();

                ServiceMethodExecutedEvent.builder()
                        .invocationId(invocationId)
                        .build()
                        .commit();

                return result;
            } catch (Throwable throwable) {
                var throwableClass = throwable.getClass();
                ServiceMethodFailedEvent.builder()
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
