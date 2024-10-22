package io.github.sibmaks.spring.jfr.controller.rest;

import io.github.sibmaks.spring.jfr.event.RestControllerInvocationEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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

    @Pointcut("@within(restController) && execution(* *(..))")
    public void restControllerMethods(RestController restController) {
    }

    @Around(value = "restControllerMethods(restController)", argNames = "joinPoint,restController")
    public Object traceRestController(ProceedingJoinPoint joinPoint, RestController restController) throws Throwable {
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        String url = null;
        String method = null;
        if (requestAttributes instanceof ServletRequestAttributes) {
            var servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            var rq = servletRequestAttributes.getRequest();
            url = rq.getRequestURI();
            method = rq.getMethod();
        }
        var event = new RestControllerInvocationEvent(
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
