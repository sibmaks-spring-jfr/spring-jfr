package io.github.sibmaks.spring.jfr.service;

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
public class ServiceRepositoryJavaFlightRecorderAspect {
    private final ContextIdProvider contextIdProvider;

    public ServiceRepositoryJavaFlightRecorderAspect(ContextIdProvider contextIdProvider) {
        this.contextIdProvider = contextIdProvider;
    }

    @Pointcut("@within(service) && execution(* *(..))")
    public void serviceMethods(Service service) {
    }

    @Around(value = "serviceMethods(service)", argNames = "joinPoint,service")
    public Object traceService(ProceedingJoinPoint joinPoint, Service service) throws Throwable {
        var contextId = contextIdProvider.getContextId();
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
