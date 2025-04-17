package io.github.sibmaks.spring.jfr.tracing.pool.jdbc;

import io.github.sibmaks.spring.jfr.event.api.tracing.pool.jdbc.connection.action.ConnectionAction;
import io.github.sibmaks.spring.jfr.event.recording.tracing.pool.jdbc.connection.action.ConnectionActionFailedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.pool.jdbc.connection.action.ConnectionActionRequestedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.pool.jdbc.connection.action.ConnectionActionSucceedEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ConnectionStatementJavaFlightRecorderAdvice implements Advice, MethodInterceptor {
    private final String connectionId;
    private final AtomicLong actionCounter;

    public ConnectionStatementJavaFlightRecorderAdvice(
            String connectionId,
            AtomicLong actionCounter
    ) {
        this.connectionId = connectionId;
        this.actionCounter = actionCounter;
    }

    @SneakyThrows
    private Object onAnyExecute(
            ConnectionAction connectionAction,
            MethodInvocation methodInvocation
    ) {
        var actionIndex = actionCounter.getAndIncrement();

        ConnectionActionRequestedEvent.builder()
                .connectionId(connectionId)
                .actionIndex(actionIndex)
                .action(connectionAction)
                .build()
                .commit();

        try {
            var rs = methodInvocation.proceed();
            ConnectionActionSucceedEvent.builder()
                    .connectionId(connectionId)
                    .actionIndex(actionIndex)
                    .build()
                    .commit();
            return rs;
        } catch (Throwable throwable) {
            var throwableClass = throwable.getClass();
            ConnectionActionFailedEvent.builder()
                    .connectionId(connectionId)
                    .actionIndex(actionIndex)
                    .exceptionClass(throwableClass.getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build()
                    .commit();
            throw throwable;
        }
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        var method = invocation.getMethod();
        var name = method.getName();
        switch (name) {
            case "execute":
                return onAnyExecute(ConnectionAction.EXECUTE, invocation);
            case "executeQuery":
                return onAnyExecute(ConnectionAction.EXECUTE_QUERY, invocation);
            case "executeUpdate":
                return onAnyExecute(ConnectionAction.EXECUTE_UPDATE, invocation);
            case "executeBatch":
                return onAnyExecute(ConnectionAction.EXECUTE_BATCH, invocation);
            case "executeLargeBatch":
                return onAnyExecute(ConnectionAction.EXECUTE_LARGE_BATCH, invocation);
            case "executeLargeUpdate":
                return onAnyExecute(ConnectionAction.EXECUTE_LARGE_UPDATE, invocation);
        }
        return invocation.proceed();
    }
}
