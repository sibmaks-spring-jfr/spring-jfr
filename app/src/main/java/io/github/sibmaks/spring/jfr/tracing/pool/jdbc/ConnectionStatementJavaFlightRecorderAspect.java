package io.github.sibmaks.spring.jfr.tracing.pool.jdbc;

import io.github.sibmaks.spring.jfr.event.api.tracing.pool.jdbc.connection.action.ConnectionAction;
import io.github.sibmaks.spring.jfr.event.recording.tracing.pool.jdbc.connection.action.ConnectionActionFailedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.pool.jdbc.connection.action.ConnectionActionRequestedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.pool.jdbc.connection.action.ConnectionActionSucceedEvent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Aspect
public class ConnectionStatementJavaFlightRecorderAspect {
    private final String connectionId;
    private final AtomicLong actionCounter;

    public ConnectionStatementJavaFlightRecorderAspect(
            String connectionId,
            AtomicLong actionCounter
    ) {
        this.connectionId = connectionId;
        this.actionCounter = actionCounter;
    }

    @Around("execution(* java.sql.Statement.execute(..)) || " +
            "execution(* java.sql.PreparedStatement.execute(..))")
    public Object onExecute(ProceedingJoinPoint pjp) throws Throwable {
        return onAnyExecute(ConnectionAction.EXECUTE, pjp);
    }

    @Around("execution(* java.sql.Statement.executeQuery(..)) || " +
            "execution(* java.sql.PreparedStatement.executeQuery(..))")
    public Object onExecuteQuery(ProceedingJoinPoint pjp) throws Throwable {
        return onAnyExecute(ConnectionAction.EXECUTE_QUERY, pjp);
    }

    @Around("execution(* java.sql.Statement.executeUpdate(..)) || " +
            "execution(* java.sql.PreparedStatement.executeUpdate(..))")
    public Object onExecuteUpdate(ProceedingJoinPoint pjp) throws Throwable {
        return onAnyExecute(ConnectionAction.EXECUTE_UPDATE, pjp);
    }

    @Around("execution(* java.sql.Statement.executeBatch(..))")
    public Object onExecuteBatch(ProceedingJoinPoint pjp) throws Throwable {
        return onAnyExecute(ConnectionAction.EXECUTE_BATCH, pjp);
    }

    @Around("execution(* java.sql.Statement.executeLargeBatch(..)) || " +
            "execution(* java.sql.PreparedStatement.executeLargeBatch(..))")
    public Object onExecuteLargeBatch(ProceedingJoinPoint pjp) throws Throwable {
        return onAnyExecute(ConnectionAction.EXECUTE_LARGE_BATCH, pjp);
    }

    @Around("execution(* java.sql.Statement.executeLargeUpdate(..)) || " +
            "execution(* java.sql.PreparedStatement.executeLargeUpdate(..))")
    public Object onExecuteLargeUpdate(ProceedingJoinPoint pjp) throws Throwable {
        return onAnyExecute(ConnectionAction.EXECUTE_LARGE_UPDATE, pjp);
    }

    private Object onAnyExecute(ConnectionAction executeUpdate, ProceedingJoinPoint pjp) throws Throwable {
        var actionIndex = actionCounter.getAndIncrement();

        ConnectionActionRequestedEvent.builder()
                .connectionId(connectionId)
                .actionIndex(actionIndex)
                .action(executeUpdate)
                .build()
                .commit();

        try {
            var rs = pjp.proceed(pjp.getArgs());
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
