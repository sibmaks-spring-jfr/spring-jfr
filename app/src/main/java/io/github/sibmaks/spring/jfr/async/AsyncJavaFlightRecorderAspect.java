package io.github.sibmaks.spring.jfr.async;

import io.github.sibmaks.spring.jfr.event.async.AsyncInvocationEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Future;

@Aspect
public class AsyncJavaFlightRecorderAspect {

    @Pointcut("@annotation(async)")
    public void asyncMethods(Async async) {
    }

    @Around(value = "asyncMethods(async)", argNames = "joinPoint,async")
    public Object traceAsyncMethods(ProceedingJoinPoint joinPoint, Async async) throws Throwable {
        var event = new AsyncInvocationEvent(
                joinPoint.getSignature().toString()
        );
        event.begin();
        Object result;
        try {
            result = joinPoint.proceed();

            if (result instanceof Future<?>) {
                return new InstrumentedFuture<>((Future<?>) result, event);
            } else {
                event.commit();
            }
        } catch (Throwable throwable) {
            event.setException(throwable.toString());
            event.commit();
            throw throwable;
        }

        return result;
    }
}
