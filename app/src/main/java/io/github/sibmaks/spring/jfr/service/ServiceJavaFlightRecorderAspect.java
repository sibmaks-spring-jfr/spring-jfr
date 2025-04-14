package io.github.sibmaks.spring.jfr.service;

import io.github.sibmaks.spring.jfr.Internal;
import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.service.ServiceMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.service.ServiceMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.service.ServiceMethodFailedEvent;
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
@Internal
public class ServiceJavaFlightRecorderAspect {
    private final String contextId;

    public ServiceJavaFlightRecorderAspect(ContextIdProvider contextIdProvider) {
        this.contextId = contextIdProvider.getContextId();
    }

    @Pointcut("@within(org.springframework.stereotype.Service) && " +
            "execution(* *(..)) && " +
            "!execution(void init(..)) && " +
            "!execution(void destroy(..)) && " +
            "!@within(io.github.sibmaks.spring.jfr.Internal)"
    )
    public void serviceMethods() {
    }

    @Around(value = "serviceMethods()", argNames = "joinPoint")
    public Object traceService(ProceedingJoinPoint joinPoint) throws Throwable {
        var correlationId = InvocationContext.getTraceId();
        var invocationId = InvocationContext.startTrace();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        ServiceMethodCalledEvent.builder()
                .correlationId(correlationId)
                .contextId(contextId)
                .invocationId(invocationId)
                .className(methodSignature.getDeclaringType().getCanonicalName())
                .methodName(methodSignature.getName())
                .build()
                .commit();

        try {
            var args = joinPoint.getArgs();
            var result = joinPoint.proceed(args);

            ServiceMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build()
                    .commit();

            return result;
        } catch (Throwable throwable) {
            ServiceMethodFailedEvent.builder()
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
