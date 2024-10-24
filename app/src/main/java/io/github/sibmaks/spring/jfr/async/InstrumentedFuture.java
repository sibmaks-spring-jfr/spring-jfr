package io.github.sibmaks.spring.jfr.async;


import io.github.sibmaks.spring.jfr.event.async.AsyncInvocationEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class InstrumentedFuture<T> implements Future<T> {

    private final Future<T> originalFuture;
    private final AsyncInvocationEvent asyncEvent;

    public InstrumentedFuture(Future<T> originalFuture, AsyncInvocationEvent asyncEvent) {
        this.originalFuture = originalFuture;
        this.asyncEvent = asyncEvent;
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
        try {
            return originalFuture.get();
        } finally {
            asyncEvent.commit();
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return originalFuture.get(timeout, unit);
        } finally {
            asyncEvent.commit();
        }
    }
}