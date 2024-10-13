package com.github.sibmaks.spring.jfr.config;

import com.github.sibmaks.spring.jfr.JavaFlightRecorderBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaFlightRecorderConfiguration {

    @Bean
    public static JavaFlightRecorderBeanPostProcessor beanPostProcessor() {
        return new JavaFlightRecorderBeanPostProcessor();
    }

}
