package io.github.sibmaks.spring.jfr.tracing.jpa;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.tracing.GenericAspectBeanPostProcessor;
import io.github.sibmaks.spring.jfr.tracing.service.ServiceJavaFlightRecorderAspect;
import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.Aspect;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.27
 */
public class JpaRepositoryJavaFlightRecorderBeanPostProcessor extends GenericAspectBeanPostProcessor {
    protected final String contextId;
    protected final JavaFlightRecorderRecordCounter flightRecorderRecordCounter;

    public JpaRepositoryJavaFlightRecorderBeanPostProcessor(
            String contextId,
            List<String> filters,
            JavaFlightRecorderRecordCounter flightRecorderRecordCounter
    ) {
        super(filters);
        this.contextId = contextId;
        this.flightRecorderRecordCounter = flightRecorderRecordCounter;
    }

    @Override
    protected Advice buildAspect(Object bean, Class<?> type) {
        return new JpaRepositoryJavaFlightRecorderAspect(type.getName(), contextId, flightRecorderRecordCounter);
    }
}
