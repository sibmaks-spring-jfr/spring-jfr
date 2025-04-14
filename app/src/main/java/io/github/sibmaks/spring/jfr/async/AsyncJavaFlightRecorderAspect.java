package io.github.sibmaks.spring.jfr.async;

import io.github.sibmaks.spring.jfr.Internal;
import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.async.AsyncMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.async.AsyncMethodFailedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;
import java.util.concurrent.Future;

@Aspect
@Internal
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
        var correlationId = InvocationContext.getTraceId();
        var invocationId = UUID.randomUUID().toString();

        AsyncMethodCalledEvent.builder()
                .contextId(contextId)
                .correlationId(correlationId)
                .invocationId(invocationId)
                .className(methodSignature.getDeclaringType().getCanonicalName())
                .methodName(methodSignature.getName())
                .build()
                .commit();
        Object result;
        try {
            var args = joinPoint.getArgs();
            result = joinPoint.proceed(args);

            if (result instanceof Future<?>) {
                return new InstrumentedFuture<>((Future<?>) result, invocationId);
            }
        } catch (Throwable throwable) {
            AsyncMethodFailedEvent.builder()
                    .invocationId(invocationId)
                    .exceptionClass(throwable.getClass().getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build()
                    .commit();
            throw throwable;
        }

        return result;
    }
}
