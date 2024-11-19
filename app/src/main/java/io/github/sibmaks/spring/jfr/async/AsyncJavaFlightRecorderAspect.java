package io.github.sibmaks.spring.jfr.async;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.async.AsyncInvocationEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Future;

@Aspect
public class AsyncJavaFlightRecorderAspect {
    private final ContextIdProvider contextIdProvider;

    public AsyncJavaFlightRecorderAspect(ContextIdProvider contextIdProvider) {
        this.contextIdProvider = contextIdProvider;
    }

    @Pointcut("@annotation(async)")
    public void asyncMethods(Async async) {
    }

    @Around(value = "asyncMethods(async)", argNames = "joinPoint,async")
    public Object traceAsyncMethods(ProceedingJoinPoint joinPoint, Async async) throws Throwable {
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        var contextId = contextIdProvider.getContextId();
        var event = AsyncInvocationEvent.builder()
                .contextId(contextId)
                .className(methodSignature.getDeclaringType().getCanonicalName())
                .methodName(methodSignature.getName())
                .build();
        event.begin();
        Object result;
        try {
            var args = joinPoint.getArgs();
            result = joinPoint.proceed(args);

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
