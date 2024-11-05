package io.github.sibmaks.spring.jfr.controller;

import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.controller.ControllerMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.controller.ControllerMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.controller.ControllerMethodFailedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
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
public class ControllerJavaFlightRecorderAspect {

    @Pointcut("@within(controller) && execution(* *(..))")
    public void controllerMethods(Controller controller) {
    }

    @Around(value = "controllerMethods(controller)", argNames = "joinPoint,controller")
    public Object traceRestController(ProceedingJoinPoint joinPoint, Controller controller) throws Throwable {
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

        var event = ControllerMethodCalledEvent.builder()
                .invocationId(invocationId)
                .className(methodSignature.getDeclaringType().getCanonicalName())
                .methodName(methodSignature.getName())
                .rest(false)
                .method(method)
                .url(url)
                .build();
        event.commit();

        try {
            var result = joinPoint.proceed();

            var finishedEvent = ControllerMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build();
            finishedEvent.commit();

            return result;
        } catch (Throwable throwable) {
            var failEvent = ControllerMethodFailedEvent.builder()
                    .invocationId(invocationId)
                    .exceptionClass(throwable.getClass().getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build();
            failEvent.commit();

            throw throwable;
        } finally {
            InvocationContext.stopTrace(invocationId);
        }
    }
}
