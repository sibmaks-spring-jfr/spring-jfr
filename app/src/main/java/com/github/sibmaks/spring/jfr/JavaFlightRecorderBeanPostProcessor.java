package com.github.sibmaks.spring.jfr;

import com.github.sibmaks.spring.jfr.event.BeanRegisteredEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JavaFlightRecorderBeanPostProcessor implements BeanPostProcessor {
    private final Map<String, BeanRegisteredEvent> beanNameToRegisteredEvent;

    public JavaFlightRecorderBeanPostProcessor() {
        this.beanNameToRegisteredEvent = new ConcurrentHashMap<>();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        var event = new BeanRegisteredEvent(beanName);
        beanNameToRegisteredEvent.put(beanName, event);
        event.begin();
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        var event = beanNameToRegisteredEvent.get(beanName);
        event.commit();
        return bean;
    }
}
