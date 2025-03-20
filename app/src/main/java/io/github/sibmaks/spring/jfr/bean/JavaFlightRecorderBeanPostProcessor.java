package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.recording.bean.PostProcessAfterInitializationEvent;
import io.github.sibmaks.spring.jfr.event.recording.bean.PostProcessBeforeDestructionEvent;
import io.github.sibmaks.spring.jfr.event.recording.bean.PostProcessBeforeInitializationEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

public class JavaFlightRecorderBeanPostProcessor implements DestructionAwareBeanPostProcessor {
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

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        var contextId = contextIdProvider.getContextId();
        PostProcessBeforeDestructionEvent.builder()
                .contextId(contextId)
                .beanName(beanName)
                .build()
                .commit();
    }

    @Override
    public boolean requiresDestruction(Object o) {
        return true;
    }
}
