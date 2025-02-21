package io.github.sibmaks.spring.jfr.config;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderObjectRegistry;
import io.github.sibmaks.spring.jfr.async.AsyncJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.bean.JavaFlightRecorderBeanDefinitionEventProducer;
import io.github.sibmaks.spring.jfr.bean.JavaFlightRecorderBeanPostProcessor;
import io.github.sibmaks.spring.jfr.bean.JavaFlightRecorderMergedBeanDefinitionEventProducer;
import io.github.sibmaks.spring.jfr.bean.JavaFlightRecorderResolveDependencyEventProducer;
import io.github.sibmaks.spring.jfr.component.ComponentRepositoryJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.controller.ControllerJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.controller.rest.RestControllerJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.core.ContextIdProviderImpl;
import io.github.sibmaks.spring.jfr.core.JavaFlightRecorderConditional;
import io.github.sibmaks.spring.jfr.core.JavaFlightRecorderProperty;
import io.github.sibmaks.spring.jfr.jpa.JpaRepositoryJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.pool.jdbc.JavaFlightRecorderHikariDataSourceAspect;
import io.github.sibmaks.spring.jfr.pool.jdbc.JavaFlightRecorderHikariDataSourceRegister;
import io.github.sibmaks.spring.jfr.scheduler.SchedulerJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.service.ServiceRepositoryJavaFlightRecorderAspect;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Java Flight recorder configuration.
 *
 * @author sibmaks
 * @since 0.0.4
 */
@Configuration
public class JavaFlightRecorderConfiguration {

    @Bean("javaFlightRecorderContextIdProvider")
    public static ContextIdProviderImpl javaFlightRecorderContextIdProvider(
            ApplicationContext applicationContext
    ) {
        return new ContextIdProviderImpl(applicationContext);
    }

    @Bean("javaFlightRecorderObjectRegistry")
    public static JavaFlightRecorderObjectRegistry javaFlightRecorderObjectRegistry() {
        return new JavaFlightRecorderObjectRegistry();
    }

    @Bean
    @JavaFlightRecorderConditional(
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.beans-creation.enabled",
                            value = "true"
                    )
            }
    )
    public static JavaFlightRecorderBeanPostProcessor javaFlightRecorderBeanPostProcessor(
            ContextIdProvider contextIdProvider
    ) {
        return new JavaFlightRecorderBeanPostProcessor(contextIdProvider);
    }

    @Bean
    @JavaFlightRecorderConditional(
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.bean-definitions.enabled",
                            value = "true"
                    )
            }
    )

    public static JavaFlightRecorderBeanDefinitionEventProducer javaFlightRecorderBeanDefinitionEventProducer(
            ContextIdProvider contextIdProvider
    ) {
        return new JavaFlightRecorderBeanDefinitionEventProducer(contextIdProvider);
    }

    @Bean
    @JavaFlightRecorderConditional(
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.bean-definitions.enabled",
                            value = "true"
                    )
            }
    )

    public static JavaFlightRecorderMergedBeanDefinitionEventProducer javaFlightRecorderMergedBeanDefinitionEventProducer(
            ContextIdProvider contextIdProvider,
            ConfigurableListableBeanFactory beanFactory
    ) {
        return new JavaFlightRecorderMergedBeanDefinitionEventProducer(contextIdProvider, beanFactory);
    }

    @Bean
    @JavaFlightRecorderConditional(
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.bean-definitions.enabled",
                            value = "true"
                    )
            }
    )

    public static JavaFlightRecorderResolveDependencyEventProducer javaFlightRecorderResolveDependencyEventProducer(
            ContextIdProvider contextIdProvider
    ) {
        return new JavaFlightRecorderResolveDependencyEventProducer(contextIdProvider);
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "org.springframework.data.jpa.repository.JpaRepository",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.jpa-repository.enabled",
                            value = "true"
                    )
            }
    )
    public static JpaRepositoryJavaFlightRecorderAspect jpaRepositoryJavaFlightRecorderAspect(
            ContextIdProvider contextIdProvider
    ) {
        return new JpaRepositoryJavaFlightRecorderAspect(contextIdProvider);
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "org.springframework.stereotype.Controller",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.controller.enabled",
                            value = "true"
                    )
            }
    )
    public static ControllerJavaFlightRecorderAspect controllerJavaFlightRecorderAspect(
            ContextIdProvider contextIdProvider
    ) {
        return new ControllerJavaFlightRecorderAspect(contextIdProvider);
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "org.springframework.web.bind.annotation.RestController",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.rest-controller.enabled",
                            value = "true"
                    )
            }
    )
    public static RestControllerJavaFlightRecorderAspect restControllerJavaFlightRecorderAspect(
            ContextIdProvider contextIdProvider
    ) {
        return new RestControllerJavaFlightRecorderAspect(contextIdProvider);
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "org.springframework.scheduling.annotation.Scheduled",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.scheduler.enabled",
                            value = "true"
                    )
            }
    )
    public static SchedulerJavaFlightRecorderAspect schedulerJavaFlightRecorderAspect(
            ContextIdProvider contextIdProvider
    ) {
        return new SchedulerJavaFlightRecorderAspect(contextIdProvider);
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "org.springframework.scheduling.annotation.Async",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.async.enabled",
                            value = "true"
                    )
            }
    )
    public static AsyncJavaFlightRecorderAspect asyncJavaFlightRecorderAspect(
            ContextIdProvider contextIdProvider
    ) {
        return new AsyncJavaFlightRecorderAspect(
                contextIdProvider
        );
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "org.springframework.stereotype.Component",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.component.enabled",
                            value = "true"
                    )
            }
    )
    public static ComponentRepositoryJavaFlightRecorderAspect componentJavaFlightRecorderAspect(
            ContextIdProvider contextIdProvider
    ) {
        return new ComponentRepositoryJavaFlightRecorderAspect(contextIdProvider);
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "org.springframework.stereotype.Service",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.service.enabled",
                            value = "true"
                    )
            }
    )
    public static ServiceRepositoryJavaFlightRecorderAspect serviceJavaFlightRecorderAspect(
            ContextIdProvider contextIdProvider
    ) {
        return new ServiceRepositoryJavaFlightRecorderAspect(contextIdProvider);
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "com.zaxxer.hikari.HikariDataSource",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.pool-jdbc.enabled",
                            value = "true"
                    )
            }
    )

    public static JavaFlightRecorderHikariDataSourceAspect javaFlightRecorderHikariDataSourceAspect(
            ContextIdProvider contextIdProvider,
            JavaFlightRecorderObjectRegistry objectRegistry
    ) {
        return new JavaFlightRecorderHikariDataSourceAspect(contextIdProvider, objectRegistry);
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "com.zaxxer.hikari.HikariDataSource",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.pool-jdbc.enabled",
                            value = "true"
                    )
            }
    )

    public static JavaFlightRecorderHikariDataSourceRegister javaFlightRecorderHikariDataSourceRegister(
            ContextIdProvider contextIdProvider,
            JavaFlightRecorderObjectRegistry objectRegistry
    ) {
        return new JavaFlightRecorderHikariDataSourceRegister(contextIdProvider, objectRegistry);
    }

}
