package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.publish.bean.PostProcessAfterInitializationEvent;
import io.github.sibmaks.spring.jfr.event.publish.bean.PostProcessBeforeInitializationEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class JavaFlightRecorderBeanPostProcessor implements BeanPostProcessor {
    private final ContextIdProvider contextIdProvider;

    public JavaFlightRecorderBeanPostProcessor(ContextIdProvider contextIdProvider) {
        this.contextIdProvider = contextIdProvider;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        var contextId = contextIdProvider.getContextId();
        PostProcessBeforeInitializationEvent.builder()
                .contextId(contextId)
                .beanName(beanName)
                .build()
                .commit();
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        var contextId = contextIdProvider.getContextId();
        PostProcessAfterInitializationEvent.builder()
                .contextId(contextId)
                .beanName(beanName)
                .build()
                .commit();
        return bean;
    }
}
