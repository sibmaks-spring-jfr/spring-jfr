package io.github.sibmaks.spring.jfr.tracing.controller.rest;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.tracing.GenericAspectBeanPostProcessor;
import io.github.sibmaks.spring.jfr.tracing.controller.ControllerJavaFlightRecorderAspect;
import org.aspectj.lang.annotation.Aspect;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.27
 */
@Aspect
public class RestControllerJavaFlightRecorderBeanPostProcessor extends GenericAspectBeanPostProcessor {
    protected final String contextId;
    protected final JavaFlightRecorderRecordCounter flightRecorderRecordCounter;

    public RestControllerJavaFlightRecorderBeanPostProcessor(
            String contextId,
            List<String> filters,
            JavaFlightRecorderRecordCounter flightRecorderRecordCounter
    ) {
        super(filters);
        this.contextId = contextId;
        this.flightRecorderRecordCounter = flightRecorderRecordCounter;
    }

    @Override
    protected Object buildAspect(Object bean, Class<?> type) {
        return new RestControllerJavaFlightRecorderAspect(type.getName(), contextId, flightRecorderRecordCounter);
    }
}
