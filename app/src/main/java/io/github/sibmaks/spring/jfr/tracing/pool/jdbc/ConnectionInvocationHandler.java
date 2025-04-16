package io.github.sibmaks.spring.jfr.tracing.pool.jdbc;

import io.github.sibmaks.spring.jfr.event.api.tracing.pool.jdbc.connection.action.ConnectionAction;
import io.github.sibmaks.spring.jfr.event.recording.tracing.pool.jdbc.connection.ConnectionTransactionLevelSetEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.pool.jdbc.connection.action.ConnectionActionFailedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.pool.jdbc.connection.action.ConnectionActionRequestedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.pool.jdbc.connection.action.ConnectionActionSucceedEvent;
import lombok.AllArgsConstructor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author sibmaks
 * @since 0.0.22
 */
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
            case "createStatement":
            case "prepareStatement":
            case "prepareCall": {
                var statement = method.invoke(realConnection, args);
                var proxyFactory = new AspectJProxyFactory(statement);
                proxyFactory.addAspect(new ConnectionStatementJavaFlightRecorderAspect(connectionId, actionCounter));
                return proxyFactory.getProxy();
            }
            case "setTransactionIsolation": {
                ConnectionTransactionLevelSetEvent.builder()
                        .connectionId(connectionId)
                        .transactionLevel(realConnection.getTransactionIsolation())
                        .build()
                        .commit();
                break;
            }
        }
        return method.invoke(realConnection, args);
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

}
