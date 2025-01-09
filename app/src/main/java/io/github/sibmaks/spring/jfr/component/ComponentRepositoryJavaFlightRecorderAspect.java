package io.github.sibmaks.spring.jfr.component;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.publish.component.ComponentMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.publish.component.ComponentMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.publish.component.ComponentMethodFailedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Spring Java Flight recorder {@link Component} invocation aspect.
 *
 * @author sibmaks
 * @since 0.0.10
 */
@Aspect
public class ComponentRepositoryJavaFlightRecorderAspect {
    private final ContextIdProvider contextIdProvider;

    public ComponentRepositoryJavaFlightRecorderAspect(ContextIdProvider contextIdProvider) {
        this.contextIdProvider = contextIdProvider;
    }

    @Pointcut("@within(component) && execution(* *(..))")
    public void componentMethods(Component component) {
    }

    @Around(value = "componentMethods(component)", argNames = "joinPoint,component")
    public Object traceComponent(ProceedingJoinPoint joinPoint, Component component) throws Throwable {
        var contextId = contextIdProvider.getContextId();
        var correlationId = InvocationContext.getTraceId();
        var invocationId = InvocationContext.startTrace();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        ComponentMethodCalledEvent.builder()
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

            ComponentMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build()
                    .commit();

            return result;
        } catch (Throwable throwable) {
            ComponentMethodFailedEvent.builder()
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
