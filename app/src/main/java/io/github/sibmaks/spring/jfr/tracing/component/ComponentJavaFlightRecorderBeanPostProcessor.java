package io.github.sibmaks.spring.jfr.tracing.component;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.tracing.GenericAspectBeanPostProcessor;
import org.aopalliance.aop.Advice;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Spring Java Flight recorder {@link Component} invocation aspect.
 *
 * @author sibmaks
 * @since 0.0.10
 */
public class ComponentJavaFlightRecorderBeanPostProcessor extends GenericAspectBeanPostProcessor {
    protected final String contextId;
    protected final JavaFlightRecorderRecordCounter flightRecorderRecordCounter;

    public ComponentJavaFlightRecorderBeanPostProcessor(
            String contextId,
            List<String> filters,
            JavaFlightRecorderRecordCounter flightRecorderRecordCounter
    ) {
        super(filters);
        this.contextId = contextId;
        this.flightRecorderRecordCounter = flightRecorderRecordCounter;
    }

    @Override
    protected Advice buildAdvice(Object bean, Class<?> type) {
        return new ComponentJavaFlightRecorderAdvice(type.getName(), contextId, flightRecorderRecordCounter);
    }

    @Override
    protected boolean isAspectBean(Object bean, Class<?> type, String beanName) {
        return AnnotatedElementUtils.hasAnnotation(type, Component.class) &&
                !AnnotatedElementUtils.hasAnnotation(type, Controller.class) &&
                !AnnotatedElementUtils.hasAnnotation(type, RestController.class) &&
                !AnnotatedElementUtils.hasAnnotation(type, Service.class) &&
                !AnnotatedElementUtils.hasAnnotation(type, Repository.class) &&
                !AnnotatedElementUtils.hasAnnotation(type, Configuration.class);
    }

}
