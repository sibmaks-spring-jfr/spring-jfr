package io.github.sibmaks.spring.jfr.config;

import io.github.sibmaks.spring.jfr.core.JavaFlightRecorderConditional;
import io.github.sibmaks.spring.jfr.core.JavaFlightRecorderProperty;
import io.github.sibmaks.spring.jfr.async.AsyncJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.bean.JavaFlightRecorderBeanDefinitionEventProducer;
import io.github.sibmaks.spring.jfr.bean.JavaFlightRecorderBeanPostProcessor;
import io.github.sibmaks.spring.jfr.controller.ControllerJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.controller.rest.RestControllerJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.jpa.JpaRepositoryJavaFlightRecorderAspect;
import io.github.sibmaks.spring.jfr.scheduler.SchedulerJavaFlightRecorderAspect;
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
            ApplicationContext applicationContext
    ) {
        return new JavaFlightRecorderBeanPostProcessor(applicationContext);
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
            ApplicationContext applicationContext,
            ConfigurableListableBeanFactory beanFactory
    ) {
        return new JavaFlightRecorderBeanDefinitionEventProducer(applicationContext, beanFactory);
    }

    @Bean
    @JavaFlightRecorderConditional(
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.jpa-repository.enabled",
                            value = "true"
                    )
            }
    )
    public static JpaRepositoryJavaFlightRecorderAspect jpaRepositoryJavaFlightRecorderAspect(
            ApplicationContext applicationContext
    ) {
        return new JpaRepositoryJavaFlightRecorderAspect(applicationContext);
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
            ApplicationContext applicationContext
    ) {
        return new ControllerJavaFlightRecorderAspect(applicationContext);
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
            ApplicationContext applicationContext
    ) {
        return new RestControllerJavaFlightRecorderAspect(applicationContext);
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
            ApplicationContext applicationContext
    ) {
        return new SchedulerJavaFlightRecorderAspect(applicationContext);
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
            ApplicationContext applicationContext
    ) {
        return new AsyncJavaFlightRecorderAspect(
                applicationContext
        );
    }

}
