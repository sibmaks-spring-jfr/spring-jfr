package io.github.sibmaks.spring.jfr.tracing.pool.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import io.github.sibmaks.spring.jfr.JavaFlightRecorderObjectRegistry;
import io.github.sibmaks.spring.jfr.event.recording.tracing.pool.jdbc.DataSourcePoolRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

@Slf4j
public class JavaFlightRecorderHikariDataSourceRegister implements DestructionAwareBeanPostProcessor {
    private final String contextId;
    private final JavaFlightRecorderObjectRegistry javaFlightRecorderObjectRegistry;

    public JavaFlightRecorderHikariDataSourceRegister(
            String contextId,
            JavaFlightRecorderObjectRegistry javaFlightRecorderObjectRegistry
    ) {
        this.contextId = contextId;
        this.javaFlightRecorderObjectRegistry = javaFlightRecorderObjectRegistry;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof HikariDataSource)) {
            return bean;
        }

        var hikariDataSource = (HikariDataSource) bean;

        DataSourcePoolRegisteredEvent.builder()
                .contextId(contextId)
                .poolId(javaFlightRecorderObjectRegistry.registerObject(bean, beanName))
                .poolName(hikariDataSource.getPoolName())
                .connectionTimeout(hikariDataSource.getConnectionTimeout())
                .idleTimeout(hikariDataSource.getIdleTimeout())
                .leakDetectionThreshold(hikariDataSource.getLeakDetectionThreshold())
                .maxLifetime(hikariDataSource.getMaxLifetime())
                .maxPoolSize(hikariDataSource.getMaximumPoolSize())
                .minimumIdle(hikariDataSource.getMinimumIdle())
                .validationTimeout(hikariDataSource.getValidationTimeout())
                .keepaliveTime(hikariDataSource.getKeepaliveTime())
                .build()
                .commit();

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        javaFlightRecorderObjectRegistry.remove(bean);
    }
}