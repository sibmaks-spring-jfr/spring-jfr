package io.github.sibmaks.spring.jfr.tracing.component;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.tracing.GenericAspectBeanPostProcessor;
import org.aopalliance.aop.Advice;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.sibmaks.spring.jfr.bean.BeanDefinitions.hasAnnotation;

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
        return hasAnnotation(type, "org.springframework.stereotype.Component") &&
                !hasAnnotation(type, "org.springframework.stereotype.Controller") &&
                !hasAnnotation(type, "org.springframework.web.bind.annotation.RestController") &&
                !hasAnnotation(type, "org.springframework.stereotype.Service") &&
                !hasAnnotation(type, "org.springframework.stereotype.Repository") &&
                !hasAnnotation(type, "org.springframework.context.annotation.Configuration");
    }
}
