package io.github.sibmaks.spring.jfr.pool.jdbc;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.recording.pool.jdbc.connection.ConnectionRequestedEvent;
import io.github.sibmaks.spring.jfr.event.recording.pool.jdbc.connection.ConnectionTransactionLevelSetEvent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.UUID;

@Slf4j
@Aspect
public class JavaFlightRecorderHikariDataSourceAspect {
    private final String contextId;

    public JavaFlightRecorderHikariDataSourceAspect(ContextIdProvider contextIdProvider) {
        this.contextId = contextIdProvider.getContextId();
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

        ConnectionTransactionLevelSetEvent.builder()
                .connectionId(connectionId)
                .transactionLevel(realConnection.getTransactionIsolation())
                .build()
                .commit();

        return proxiedConnection;
    }
}