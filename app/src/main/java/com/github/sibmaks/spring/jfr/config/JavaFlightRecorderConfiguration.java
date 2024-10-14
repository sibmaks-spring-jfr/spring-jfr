package com.github.sibmaks.spring.jfr.config;

import com.github.sibmaks.spring.jfr.JavaFlightRecorderConditional;
import com.github.sibmaks.spring.jfr.JavaFlightRecorderProperty;
import com.github.sibmaks.spring.jfr.async.AsyncJavaFlightRecorderAspect;
import com.github.sibmaks.spring.jfr.bean.JavaFlightRecorderBeanPostProcessor;
import com.github.sibmaks.spring.jfr.controller.ControllerJavaFlightRecorderAspect;
import com.github.sibmaks.spring.jfr.controller.rest.RestControllerJavaFlightRecorderAspect;
import com.github.sibmaks.spring.jfr.jpa.JpaRepositoryJavaFlightRecorderAspect;
import com.github.sibmaks.spring.jfr.scheduler.SchedulerJavaFlightRecorderAspect;
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
                            value = "true",
                            matchIfMissing = true
                    )
            }
    )
    public static JavaFlightRecorderBeanPostProcessor javaFlightRecorderBeanPostProcessor() {
        return new JavaFlightRecorderBeanPostProcessor();
    }

    @Bean
    @JavaFlightRecorderConditional(
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.jpa-repository.enabled",
                            value = "true",
                            matchIfMissing = true
                    )
            }
    )
    public static JpaRepositoryJavaFlightRecorderAspect jpaRepositoryJavaFlightRecorderAspect() {
        return new JpaRepositoryJavaFlightRecorderAspect();
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "org.springframework.stereotype.Controller",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.controller.enabled",
                            value = "true",
                            matchIfMissing = true
                    )
            }
    )
    public static ControllerJavaFlightRecorderAspect controllerJavaFlightRecorderAspect() {
        return new ControllerJavaFlightRecorderAspect();
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "org.springframework.web.bind.annotation.RestController",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.rest-controller.enabled",
                            value = "true",
                            matchIfMissing = true
                    )
            }
    )
    public static RestControllerJavaFlightRecorderAspect restControllerJavaFlightRecorderAspect() {
        return new RestControllerJavaFlightRecorderAspect();
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "org.springframework.scheduling.annotation.Scheduled",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.scheduler.enabled",
                            value = "true",
                            matchIfMissing = true
                    )
            }
    )
    public static SchedulerJavaFlightRecorderAspect schedulerJavaFlightRecorderAspect() {
        return new SchedulerJavaFlightRecorderAspect();
    }

    @Bean
    @JavaFlightRecorderConditional(
            requiredClasses = "org.springframework.scheduling.annotation.Async",
            properties = {
                    @JavaFlightRecorderProperty(
                            key = "spring.jfr.instrumentation.scheduler.enabled",
                            value = "true",
                            matchIfMissing = true
                    )
            }
    )
    public static AsyncJavaFlightRecorderAspect asyncJavaFlightRecorderAspect() {
        return new AsyncJavaFlightRecorderAspect();
    }

}
