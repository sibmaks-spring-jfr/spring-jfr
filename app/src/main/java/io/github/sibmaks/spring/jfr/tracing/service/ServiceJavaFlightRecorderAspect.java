package io.github.sibmaks.spring.jfr.tracing.service;

import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.tracing.service.ServiceMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.service.ServiceMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.service.ServiceMethodFailedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Service;

/**
 * Spring Java Flight recorder {@link Service} invocation aspect.
 *
 * @author sibmaks
 * @since 0.0.10
 */
@Aspect
public class ServiceJavaFlightRecorderAspect {
    private final String contextId;

    public ServiceJavaFlightRecorderAspect(String contextId) {
        this.contextId = contextId;
    }

    @Pointcut("@within(org.springframework.stereotype.Service) && " +
            "execution(* *(..)) && " +
            "!execution(void init(..)) && " +
            "!execution(void destroy(..)) && " +
            "!@within(io.github.sibmaks.spring.jfr.event.api.tracing.IgnoreTracing)"
    )
    public void serviceMethods() {
    }

    @Around(value = "serviceMethods()", argNames = "joinPoint")
    public Object traceService(ProceedingJoinPoint joinPoint) throws Throwable {
        var correlationId = InvocationContext.getTraceId();
        var invocationId = InvocationContext.startTrace();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        var declaringType = methodSignature.getDeclaringType();
        ServiceMethodCalledEvent.builder()
                .correlationId(correlationId)
                .contextId(contextId)
                .invocationId(invocationId)
                .className(declaringType.getCanonicalName())
                .methodName(methodSignature.getName())
                .build()
                .commit();

        try {
            var result = joinPoint.proceed();

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
        } finally {
            InvocationContext.stopTrace(invocationId);
        }
    }
}
