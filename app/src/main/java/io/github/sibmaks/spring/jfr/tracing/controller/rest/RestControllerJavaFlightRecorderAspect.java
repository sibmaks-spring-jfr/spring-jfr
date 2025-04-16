package io.github.sibmaks.spring.jfr.tracing.controller.rest;

import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.tracing.controller.ControllerMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.controller.ControllerMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.controller.ControllerMethodFailedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Spring Java Flight recorder {@link RestController} invocation aspect.
 *
 * @author sibmaks
 * @since 0.0.4
 */
@Aspect
public class RestControllerJavaFlightRecorderAspect {
    private final String contextId;

    public RestControllerJavaFlightRecorderAspect(String contextId) {
        this.contextId = contextId;
    }

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) && " +
            "execution(* *(..)) && " +
            "!execution(void init(..)) && " +
            "!execution(void destroy(..))")
    public void restControllerMethods() {
    }

    @Around(value = "restControllerMethods()", argNames = "joinPoint")
    public Object traceRestController(ProceedingJoinPoint joinPoint) throws Throwable {
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
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        var declaringType = methodSignature.getDeclaringType();
        ControllerMethodCalledEvent.builder()
                .contextId(contextId)
                .invocationId(invocationId)
                .className(declaringType.getCanonicalName())
                .methodName(methodSignature.getName())
                .rest(true)
                .httpMethod(method)
                .url(url)
                .build()
                .commit();

        try {
            var result = joinPoint.proceed();

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
        } finally {
            InvocationContext.stopTrace(invocationId);
        }
    }
}
