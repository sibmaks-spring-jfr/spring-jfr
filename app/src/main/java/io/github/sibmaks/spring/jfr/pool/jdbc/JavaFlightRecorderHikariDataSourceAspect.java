package io.github.sibmaks.spring.jfr.pool.jdbc;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.api.pool.jdbc.connection.action.ConnectionAction;
import io.github.sibmaks.spring.jfr.event.recording.pool.jdbc.connection.ConnectionRequestedEvent;
import io.github.sibmaks.spring.jfr.event.recording.pool.jdbc.connection.action.ConnectionActionFailedEvent;
import io.github.sibmaks.spring.jfr.event.recording.pool.jdbc.connection.action.ConnectionActionRequestedEvent;
import io.github.sibmaks.spring.jfr.event.recording.pool.jdbc.connection.action.ConnectionActionSucceedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Aspect
public class JavaFlightRecorderHikariDataSourceAspect {
    private final String contextId;

    public JavaFlightRecorderHikariDataSourceAspect(ContextIdProvider contextIdProvider) {
        this.contextId = contextIdProvider.getContextId();
    }

    private static void connectionActionRequested(String connectionId, long actionIndex, ConnectionAction action) {
        ConnectionActionRequestedEvent.builder()
                .connectionId(connectionId)
                .actionIndex(actionIndex)
                .action(action)
                .build()
                .commit();
    }

    @Around("execution(* com.zaxxer.hikari.HikariDataSource.getConnection(..)) && target(dataSource)")
    public Object aroundGetConnection(ProceedingJoinPoint pjp, Object dataSource) throws Throwable {
        var realConnection = (Connection) pjp.proceed();
        var connectionId = UUID.randomUUID().toString();

        var proxiedConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                new ConnectionInvocationHandler(connectionId, realConnection)
        );

        ConnectionRequestedEvent.builder()
                .contextId(contextId)
                .poolId(JDBCPoolRegistry.getPoolId(dataSource))
                .connectionId(connectionId)
                .build()
                .commit();

        return proxiedConnection;
    }

    @AllArgsConstructor
    private static class ConnectionInvocationHandler implements InvocationHandler {
        private final String connectionId;
        private final Connection realConnection;
        private final AtomicLong actionCounter = new AtomicLong(1);

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            var actionIndex = actionCounter.getAndIncrement();

            if ("close".equals(method.getName())) {
                connectionActionRequested(connectionId, actionIndex, ConnectionAction.CLOSE);
            }
            if ("commit".equals(method.getName())) {
                connectionActionRequested(connectionId, actionIndex, ConnectionAction.COMMIT);
            }
            if ("rollback".equals(method.getName())) {
                connectionActionRequested(connectionId, actionIndex, ConnectionAction.ROLLBACK);
            }

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
    }
}