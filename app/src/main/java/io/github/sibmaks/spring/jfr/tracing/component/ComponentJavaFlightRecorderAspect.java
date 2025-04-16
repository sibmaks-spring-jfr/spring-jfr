package io.github.sibmaks.spring.jfr.tracing.component;

import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.tracing.component.ComponentMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.component.ComponentMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.component.ComponentMethodFailedEvent;
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
public class ComponentJavaFlightRecorderAspect {
    private final String contextId;

    public ComponentJavaFlightRecorderAspect(String contextId) {
        this.contextId = contextId;
    }

    @Pointcut(
            "@within(org.springframework.stereotype.Component) && execution(* *(..)) && " +
                    "!within(org.springframework.web.filter.GenericFilterBean+) && " +
                    "!within(org.springframework.web.filter.OncePerRequestFilter+) &&" +
                    "!within(org.springframework.beans.factory.config.BeanPostProcessor+) &&" +
                    "!within(org.springframework.beans.factory.config.BeanFactoryPostProcessor+) && " +
                    "!@within(io.github.sibmaks.spring.jfr.event.api.tracing.IgnoreTracing)"
    )
    public void componentMethods() {
    }

    @Around(value = "componentMethods()", argNames = "joinPoint")
    public Object traceComponent(ProceedingJoinPoint joinPoint) throws Throwable {
        var correlationId = InvocationContext.getTraceId();
        var invocationId = InvocationContext.startTrace();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        var declaringType = methodSignature.getDeclaringType();
        ComponentMethodCalledEvent.builder()
                .correlationId(correlationId)
                .contextId(contextId)
                .invocationId(invocationId)
                .className(declaringType.getCanonicalName())
                .methodName(methodSignature.getName())
                .build()
                .commit();

        try {
            var result = joinPoint.proceed();

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
        } finally {
            InvocationContext.stopTrace(invocationId);
        }
    }
}
