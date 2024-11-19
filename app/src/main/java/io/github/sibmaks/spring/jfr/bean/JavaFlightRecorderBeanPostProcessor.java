package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.bean.PostProcessAfterInitializationEvent;
import io.github.sibmaks.spring.jfr.event.bean.PostProcessBeforeInitializationEvent;
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
        var event = PostProcessBeforeInitializationEvent.builder()
                .contextId(contextId)
                .beanName(beanName)
                .build();
        event.commit();
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        var contextId = contextIdProvider.getContextId();
        var event = PostProcessAfterInitializationEvent.builder()
                .contextId(contextId)
                .beanName(beanName)
                .build();
        event.commit();
        return bean;
    }
}
