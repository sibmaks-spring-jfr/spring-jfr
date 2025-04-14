package io.github.sibmaks.spring.jfr.pool.jdbc;

import io.github.sibmaks.spring.jfr.Internal;
import io.github.sibmaks.spring.jfr.event.api.pool.jdbc.connection.action.ConnectionAction;
import io.github.sibmaks.spring.jfr.event.recording.pool.jdbc.connection.ConnectionTransactionLevelSetEvent;
import io.github.sibmaks.spring.jfr.event.recording.pool.jdbc.connection.action.ConnectionActionFailedEvent;
import io.github.sibmaks.spring.jfr.event.recording.pool.jdbc.connection.action.ConnectionActionRequestedEvent;
import io.github.sibmaks.spring.jfr.event.recording.pool.jdbc.connection.action.ConnectionActionSucceedEvent;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author sibmaks
 * @since 0.0.22
 */
@Internal
@AllArgsConstructor
class ConnectionInvocationHandler implements InvocationHandler {
    private final String connectionId;
    private final Connection realConnection;
    private final AtomicLong actionCounter = new AtomicLong(1);

    private static void connectionActionRequested(String connectionId, long actionIndex, ConnectionAction action) {
        ConnectionActionRequestedEvent.builder()
                .connectionId(connectionId)
                .actionIndex(actionIndex)
                .action(action)
                .build()
                .commit();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var actionIndex = actionCounter.getAndIncrement();

        switch (method.getName()) {
            case "close":
                connectionActionRequested(connectionId, actionIndex, ConnectionAction.CLOSE);
                return actionTrack(method, args, actionIndex);
            case "commit":
                connectionActionRequested(connectionId, actionIndex, ConnectionAction.COMMIT);
                return actionTrack(method, args, actionIndex);
            case "rollback":
                connectionActionRequested(connectionId, actionIndex, ConnectionAction.ROLLBACK);
                return actionTrack(method, args, actionIndex);
            default:
                return fastTrack(method, args);
        }
    }

    private Object actionTrack(Method method, Object[] args, long actionIndex)
            throws IllegalAccessException, InvocationTargetException {
        try {
            var rs = method.invoke(realConnection, args);

            ConnectionActionSucceedEvent.builder()
                    .connectionId(connectionId)
                    .actionIndex(actionIndex)
                    .build()
                    .commit();

            return rs;
        } catch (Throwable throwable) {
            ConnectionActionFailedEvent.builder()
                    .connectionId(connectionId)
                    .actionIndex(actionIndex)
                    .exceptionClass(throwable.getClass().getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build()
                    .commit();
            throw throwable;
        }
    }

    private Object fastTrack(Method method, Object[] args)
            throws IllegalAccessException, InvocationTargetException, SQLException {
        var rs = method.invoke(realConnection, args);

        if ("setTransactionIsolation".equals(method.getName())) {
            ConnectionTransactionLevelSetEvent.builder()
                    .connectionId(connectionId)
                    .transactionLevel(realConnection.getTransactionIsolation())
                    .build()
                    .commit();
        }

        return rs;
    }
}
