package io.github.sibmaks.spring.jfr.controller.rest;

import io.github.sibmaks.spring.jfr.Internal;
import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.controller.ControllerMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.controller.ControllerMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.recording.controller.ControllerMethodFailedEvent;
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
@Internal
public class RestControllerJavaFlightRecorderAspect {
    private final ContextIdProvider contextIdProvider;

    public RestControllerJavaFlightRecorderAspect(ContextIdProvider contextIdProvider) {
        this.contextIdProvider = contextIdProvider;
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
        var contextId = contextIdProvider.getContextId();
        var invocationId = InvocationContext.startTrace();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        ControllerMethodCalledEvent.builder()
                .contextId(contextId)
                .invocationId(invocationId)
                .className(methodSignature.getDeclaringType().getCanonicalName())
                .methodName(methodSignature.getName())
                .rest(true)
                .httpMethod(method)
                .url(url)
                .build()
                .commit();

        try {
            var args = joinPoint.getArgs();
            var result = joinPoint.proceed(args);

            ControllerMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build()
                    .commit();

            return result;
        } catch (Throwable throwable) {
            ControllerMethodFailedEvent.builder()
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
