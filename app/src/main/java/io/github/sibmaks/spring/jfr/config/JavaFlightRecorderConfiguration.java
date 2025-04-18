package io.github.sibmaks.spring.jfr.config;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderObjectRegistry;
import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.bean.JavaFlightRecorderBeanDefinitionEventProducer;
import io.github.sibmaks.spring.jfr.bean.JavaFlightRecorderBeanPostProcessor;
import io.github.sibmaks.spring.jfr.bean.JavaFlightRecorderMergedBeanDefinitionEventProducer;
import io.github.sibmaks.spring.jfr.bean.JavaFlightRecorderResolveDependencyEventProducer;
import io.github.sibmaks.spring.jfr.core.JavaFlightRecorderConditional;
import io.github.sibmaks.spring.jfr.core.JavaFlightRecorderProperty;
import io.github.sibmaks.spring.jfr.core.impl.ContextIdProviderImpl;
import io.github.sibmaks.spring.jfr.tracing.async.AsyncJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.tracing.component.ComponentJavaFlightRecorderBeanPostProcessor;
import io.github.sibmaks.spring.jfr.tracing.controller.ControllerJavaFlightRecorderBeanPostProcessor;
import io.github.sibmaks.spring.jfr.tracing.controller.rest.RestControllerJavaFlightRecorderBeanPostProcessor;
import io.github.sibmaks.spring.jfr.tracing.jpa.JpaRepositoryJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.tracing.kafka.consumer.KafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor;
import io.github.sibmaks.spring.jfr.tracing.pool.jdbc.JavaFlightRecorderHikariDataSourceAspect;
import io.github.sibmaks.spring.jfr.tracing.pool.jdbc.JavaFlightRecorderHikariDataSourceRegister;
import io.github.sibmaks.spring.jfr.tracing.scheduler.SchedulerJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.tracing.service.ServiceJavaFlightRecorderBeanPostProcessor;
import jdk.jfr.FlightRecorder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

/**
 * Spring Java Flight recorder configuration.
 *
 * @author sibmaks
 * @since 0.0.4
 */
@Configuration
public class JavaFlightRecorderConfiguration {

    @Bean("javaFlightRecorderContextId")
    public static String javaFlightRecorderContextId(
            ApplicationContext applicationContext
    ) {
        var contextIdProvider = new ContextIdProviderImpl(applicationContext);
        return contextIdProvider.getContextId();
    }

    @Bean("flightRecorderRecordCounter")
    public static JavaFlightRecorderRecordCounter flightRecorderRecordCounter() {
        var counter = new JavaFlightRecorderRecordCounter();
        FlightRecorder.addListener(counter);
        return counter;
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
            @Qualifier("javaFlightRecorderContextId") String contextId
    ) {
        return new JavaFlightRecorderBeanPostProcessor(contextId);
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
            @Qualifier("javaFlightRecorderContextId") String contextId
    ) {
        return new JavaFlightRecorderBeanDefinitionEventProducer(contextId);
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
            @Qualifier("javaFlightRecorderContextId") String contextId,
            ConfigurableListableBeanFactory beanFactory
    ) {
        return new JavaFlightRecorderMergedBeanDefinitionEventProducer(contextId, beanFactory);
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
            @Qualifier("javaFlightRecorderContextId") String contextId
    ) {
        return new JavaFlightRecorderResolveDependencyEventProducer(contextId);
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
    public static JpaRepositoryJavaFlightRecorderAspect jpaRepositoryJavaFlightRecorderBeanPostProcessor(
            @Qualifier("javaFlightRecorderContextId") String contextId,
            JavaFlightRecorderRecordCounter flightRecorderRecordCounter
    ) {
        return new JpaRepositoryJavaFlightRecorderAspect(contextId, flightRecorderRecordCounter);
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
    public static ControllerJavaFlightRecorderBeanPostProcessor controllerJavaFlightRecorderBeanPostProcessor(
            @Qualifier("javaFlightRecorderContextId") String contextId,
            @Value("${spring.jfr.instrumentation.controller.filters}") String rawFilters,
            JavaFlightRecorderRecordCounter flightRecorderRecordCounter
    ) {
        var filters = Optional.ofNullable(rawFilters)
                .map(it -> it.split(","))
                .map(List::of)
                .orElseGet(List::of);
        return new ControllerJavaFlightRecorderBeanPostProcessor(contextId, filters, flightRecorderRecordCounter);
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
    public static RestControllerJavaFlightRecorderBeanPostProcessor restControllerJavaFlightRecorderBeanPostProcessor(
            @Qualifier("javaFlightRecorderContextId") String contextId,
            @Value("${spring.jfr.instrumentation.rest-controller.filters}") String rawFilters,
            JavaFlightRecorderRecordCounter flightRecorderRecordCounter
    ) {
        var filters = Optional.ofNullable(rawFilters)
                .map(it -> it.split(","))
                .map(List::of)
                .orElseGet(List::of);
        return new RestControllerJavaFlightRecorderBeanPostProcessor(contextId, filters, flightRecorderRecordCounter);
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
            @Qualifier("javaFlightRecorderContextId") String contextId
    ) {
        return new SchedulerJavaFlightRecorderAspect(contextId);
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
            @Qualifier("javaFlightRecorderContextId") String contextId
    ) {
        return new AsyncJavaFlightRecorderAspect(contextId);
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
    public static ComponentJavaFlightRecorderBeanPostProcessor componentJavaFlightRecorderBeanPostProcessor(
            @Qualifier("javaFlightRecorderContextId") String contextId,
            @Value("${spring.jfr.instrumentation.component.filters}") String rawFilters,
            JavaFlightRecorderRecordCounter flightRecorderRecordCounter
    ) {
        var filters = Optional.ofNullable(rawFilters)
                .map(it -> it.split(","))
                .map(List::of)
                .orElseGet(List::of);
        return new ComponentJavaFlightRecorderBeanPostProcessor(contextId, filters, flightRecorderRecordCounter);
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
    public static ServiceJavaFlightRecorderBeanPostProcessor serviceJavaFlightRecorderBeanPostProcessor(
            @Qualifier("javaFlightRecorderContextId") String contextId,
            @Value("${spring.jfr.instrumentation.service.filters}") String rawFilters,
            JavaFlightRecorderRecordCounter flightRecorderRecordCounter
    ) {
        var filters = Optional.ofNullable(rawFilters)
                .map(it -> it.split(","))
                .map(List::of)
                .orElseGet(List::of);
        return new ServiceJavaFlightRecorderBeanPostProcessor(contextId, filters, flightRecorderRecordCounter);
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
            @Qualifier("javaFlightRecorderContextId") String contextId,
            JavaFlightRecorderObjectRegistry objectRegistry
    ) {
        return new JavaFlightRecorderHikariDataSourceAspect(contextId, objectRegistry);
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
            @Qualifier("javaFlightRecorderContextId") String contextId,
            JavaFlightRecorderObjectRegistry objectRegistry
    ) {
        return new JavaFlightRecorderHikariDataSourceRegister(contextId, objectRegistry);
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = {
                    "org.springframework.kafka.core.ConsumerFactory",
                    "org.apache.kafka.clients.consumer.Consumer"
            },
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.kafka.enabled",
                            value = "true"
                    )
            }
    )
    public static KafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor kafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor(
            @Qualifier("javaFlightRecorderContextId") String contextId,
            JavaFlightRecorderObjectRegistry objectRegistry
    ) {
        return new KafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor(contextId, objectRegistry);
    }

}
