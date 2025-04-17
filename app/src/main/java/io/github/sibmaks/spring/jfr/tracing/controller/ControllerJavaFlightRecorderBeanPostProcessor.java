package io.github.sibmaks.spring.jfr.tracing.controller;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.tracing.GenericAspectBeanPostProcessor;
import org.aopalliance.aop.Advice;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.27
 */
public class ControllerJavaFlightRecorderBeanPostProcessor extends GenericAspectBeanPostProcessor {
    protected final String contextId;
    protected final JavaFlightRecorderRecordCounter flightRecorderRecordCounter;

    public ControllerJavaFlightRecorderBeanPostProcessor(
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
        return new ControllerJavaFlightRecorderAdvice(type.getName(), contextId, flightRecorderRecordCounter);
    }

    @Override
    protected boolean isAspectBean(Object bean, Class<?> type, String beanName) {
        return AnnotatedElementUtils.hasAnnotation(type, Controller.class) &&
                !AnnotatedElementUtils.hasAnnotation(type, RestController.class);
    }
}
