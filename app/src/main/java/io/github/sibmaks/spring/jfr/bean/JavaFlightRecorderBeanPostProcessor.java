package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.event.recording.bean.PostProcessAfterInitializationEvent;
import io.github.sibmaks.spring.jfr.event.recording.bean.PostProcessBeforeDestructionEvent;
import io.github.sibmaks.spring.jfr.event.recording.bean.PostProcessBeforeInitializationEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

public class JavaFlightRecorderBeanPostProcessor implements DestructionAwareBeanPostProcessor {
    private final String contextId;

    public JavaFlightRecorderBeanPostProcessor(String contextId) {
        this.contextId = contextId;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        PostProcessBeforeInitializationEvent.builder()
                .contextId(contextId)
                .beanName(beanName)
                .build()
                .commit();
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        PostProcessAfterInitializationEvent.builder()
                .contextId(contextId)
                .beanName(beanName)
                .build()
                .commit();
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        PostProcessBeforeDestructionEvent.builder()
                .contextId(contextId)
                .beanName(beanName)
                .build()
                .commit();
    }
}
