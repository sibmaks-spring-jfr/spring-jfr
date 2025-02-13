package io.github.sibmaks.spring.jfr.pool.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.recording.pool.jdbc.DataSourcePoolRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

@Slf4j
public class JavaFlightRecorderHikariDataSourceRegister implements BeanPostProcessor {
    private final String contextId;

    public JavaFlightRecorderHikariDataSourceRegister(ContextIdProvider contextIdProvider) {
        this.contextId = contextIdProvider.getContextId();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof HikariDataSource)) {
            return bean;
        }

        var hikariDataSource = (HikariDataSource) bean;

        DataSourcePoolRegisteredEvent.builder()
                .contextId(contextId)
                .poolId(JDBCPoolRegistry.getPoolId(bean))
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
}