package io.github.sibmaks.spring.jfr.tracing.jpa;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderRecordCounter;
import io.github.sibmaks.spring.jfr.tracing.GenericAspectBeanPostProcessor;
import org.aopalliance.aop.Advice;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.repository.core.support.RepositoryFactoryInformation;
import org.springframework.stereotype.Repository;

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
    protected Class<?> getActualBeanType(Object bean) {
        if (bean instanceof RepositoryFactoryInformation<?, ?>) {
            var factoryBean = (RepositoryFactoryInformation<?, ?>) bean;
            var repositoryInformation = factoryBean.getRepositoryInformation();
            return repositoryInformation.getRepositoryInterface();
        }
        return super.getActualBeanType(bean);
    }

    @Override
    protected Advice buildAdvice(Object bean, Class<?> type) {
        return new JpaRepositoryJavaFlightRecorderAdvice(type.getName(), contextId, flightRecorderRecordCounter);
    }

    @Override
    protected boolean isAspectBean(Object bean, Class<?> type, String beanName) {
        var hasAnnotation = AnnotatedElementUtils.hasAnnotation(type, Repository.class);
        if (hasAnnotation) {
            return true;
        }
        return org.springframework.data.repository.Repository.class.isAssignableFrom(type);
    }
}
