package com.github.sibmaks.spring.jfr.config;

import com.github.sibmaks.spring.jfr.JavaFlightRecorderBeanPostProcessor;
import com.github.sibmaks.spring.jfr.OnClassConditional;
import com.github.sibmaks.spring.jfr.controller.ControllerJavaFlightRecorderAspect;
import com.github.sibmaks.spring.jfr.controller.rest.RestControllerJavaFlightRecorderAspect;
import com.github.sibmaks.spring.jfr.jpa.JpaRepositoryJavaFlightRecorderAspect;
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
    public static JavaFlightRecorderBeanPostProcessor beanPostProcessor() {
        return new JavaFlightRecorderBeanPostProcessor();
    }

    @Bean
    public static JpaRepositoryJavaFlightRecorderAspect jpaRepositoryJfrAspect() {
        return new JpaRepositoryJavaFlightRecorderAspect();
    }

    @Bean
    @OnClassConditional("org.springframework.stereotype.Controller")
    public static ControllerJavaFlightRecorderAspect controllerJfrAspect() {
        return new ControllerJavaFlightRecorderAspect();
    }

    @Bean
    @OnClassConditional("org.springframework.web.bind.annotation.RestController")
    public static RestControllerJavaFlightRecorderAspect restControllerJfrAspect() {
        return new RestControllerJavaFlightRecorderAspect();
    }

}
