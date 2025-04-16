package io.github.sibmaks.spring.jfr.tracing.async;

import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.tracing.async.AsyncMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.async.AsyncMethodFailedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.UUID;
import java.util.concurrent.Future;

@Aspect
public class AsyncJavaFlightRecorderAspect {
    private final String contextId;

    public AsyncJavaFlightRecorderAspect(String contextId) {
        this.contextId = contextId;
    }

    @Pointcut("@annotation(org.springframework.scheduling.annotation.Async)")
    public void asyncMethods() {
    }

    @Around(value = "asyncMethods()", argNames = "joinPoint")
    public Object traceAsyncMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        var correlationId = InvocationContext.getTraceId();
        var invocationId = UUID.randomUUID().toString();

        var declaringType = methodSignature.getDeclaringType();
        AsyncMethodCalledEvent.builder()
                .contextId(contextId)
                .correlationId(correlationId)
                .invocationId(invocationId)
                .className(declaringType.getCanonicalName())
                .methodName(methodSignature.getName())
                .build()
                .commit();
        Object result;
        try {
            result = joinPoint.proceed();

            if (result instanceof Future<?>) {
                return new InstrumentedFuture<>((Future<?>) result, invocationId);
            }
        } catch (Throwable throwable) {
            var throwableClass = throwable.getClass();
            AsyncMethodFailedEvent.builder()
                    .invocationId(invocationId)
                    .exceptionClass(throwableClass.getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build()
                    .commit();
            throw throwable;
        }

        return result;
    }
}
