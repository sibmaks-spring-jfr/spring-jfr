package io.github.sibmaks.spring.jfr.tracing.service;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.tracing.GenericAspectBeanPostProcessor;
import org.aopalliance.aop.Advice;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.27
 */
public class ServiceJavaFlightRecorderBeanPostProcessor extends GenericAspectBeanPostProcessor {
    protected final String contextId;
    protected final JavaFlightRecorderRecordCounter flightRecorderRecordCounter;

    public ServiceJavaFlightRecorderBeanPostProcessor(
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
        return new ServiceJavaFlightRecorderAspect(type.getName(), contextId, flightRecorderRecordCounter);
    }
}
