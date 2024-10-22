package io.github.sibmaks.spring.jfr.controller;

import io.github.sibmaks.spring.jfr.event.ControllerInvocationEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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
        var event = new ControllerInvocationEvent(
                joinPoint.getSignature().toString(),
                method,
                url
        );

        event.begin();
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            event.setException(throwable.toString());
            throw throwable;
        } finally {
            event.commit();
        }
    }
}
