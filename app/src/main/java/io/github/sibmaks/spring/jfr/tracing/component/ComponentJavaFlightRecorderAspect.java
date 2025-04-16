package io.github.sibmaks.spring.jfr.tracing.component;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.tracing.component.ComponentMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.component.ComponentMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.component.ComponentMethodFailedEvent;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Spring Java Flight recorder {@link Component} invocation aspect.
 *
 * @author sibmaks
 * @since 0.0.10
 */
@Aspect
public class ComponentJavaFlightRecorderAspect implements Advice, MethodInterceptor {
    private final String className;
    private final String contextId;
    private final JavaFlightRecorderRecordCounter flightRecorderRecordCounter;

    public ComponentJavaFlightRecorderAspect(
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

            ComponentMethodCalledEvent.builder()
                    .correlationId(correlationId)
                    .contextId(contextId)
                    .invocationId(invocationId)
                    .className(className)
                    .methodName(method.getName())
                    .build()
                    .commit();

            try {
                var result = methodInvocation.proceed();

                ComponentMethodExecutedEvent.builder()
                        .invocationId(invocationId)
                        .build()
                        .commit();

                return result;
            } catch (Throwable throwable) {
                var throwableClass = throwable.getClass();
                ComponentMethodFailedEvent.builder()
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
