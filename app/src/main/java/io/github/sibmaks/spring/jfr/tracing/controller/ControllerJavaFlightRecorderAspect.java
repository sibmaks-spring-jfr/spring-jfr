package io.github.sibmaks.spring.jfr.tracing.controller;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.tracing.controller.ControllerMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.controller.ControllerMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.controller.ControllerMethodFailedEvent;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Spring Java Flight recorder {@link Controller} invocation aspect.
 *
 * @author sibmaks
 * @since 0.0.4
 */
@Aspect
public class ControllerJavaFlightRecorderAspect implements Advice, MethodInterceptor {
    private final String className;
    private final String contextId;
    private final JavaFlightRecorderRecordCounter flightRecorderRecordCounter;

    public ControllerJavaFlightRecorderAspect(
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
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        String url = null;
        String method = null;
        if (requestAttributes instanceof ServletRequestAttributes) {
            var servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            var rq = servletRequestAttributes.getRequest();
            url = rq.getRequestURI();
            method = rq.getMethod();
        }
        var invocationId = InvocationContext.startTrace();
        try {
            var methodSignature = methodInvocation.getMethod();

            ControllerMethodCalledEvent.builder()
                    .contextId(contextId)
                    .invocationId(invocationId)
                    .className(className)
                    .methodName(methodSignature.getName())
                    .rest(false)
                    .httpMethod(method)
                    .url(url)
                    .build()
                    .commit();

            try {
                var result = methodInvocation.proceed();

                ControllerMethodExecutedEvent.builder()
                        .invocationId(invocationId)
                        .build()
                        .commit();

                return result;
            } catch (Throwable throwable) {
                var throwableClass = throwable.getClass();
                ControllerMethodFailedEvent.builder()
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
