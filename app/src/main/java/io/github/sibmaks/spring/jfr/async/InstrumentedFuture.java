package io.github.sibmaks.spring.jfr.async;


import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.publish.async.AsyncMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.publish.async.AsyncMethodFailedEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class InstrumentedFuture<T> implements Future<T> {

    private final Future<T> originalFuture;
    private final String invocationId;

    public InstrumentedFuture(Future<T> originalFuture, String invocationId) {
        this.originalFuture = originalFuture;
        this.invocationId = invocationId;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return originalFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return originalFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return originalFuture.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        InvocationContext.startTrace(invocationId);
        try {
            var rs = originalFuture.get();
            AsyncMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build()
                    .commit();
            return rs;
        } catch (Throwable throwable) {
            AsyncMethodFailedEvent.builder()
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

    @Override
    public T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        InvocationContext.startTrace(invocationId);
        try {
            var rs = originalFuture.get(timeout, unit);
            AsyncMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build()
                    .commit();
            return rs;
        } catch (Throwable throwable) {
            AsyncMethodFailedEvent.builder()
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