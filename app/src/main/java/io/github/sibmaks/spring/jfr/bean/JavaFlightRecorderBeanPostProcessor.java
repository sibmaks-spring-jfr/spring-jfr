package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.event.bean.PostProcessAfterInitializationEvent;
import io.github.sibmaks.spring.jfr.event.bean.PostProcessBeforeInitializationEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;

public class JavaFlightRecorderBeanPostProcessor implements BeanPostProcessor {
    private final ApplicationContext applicationContext;

    public JavaFlightRecorderBeanPostProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        var contextId = applicationContext.getId();
        var event = PostProcessBeforeInitializationEvent.builder()
                .contextId(contextId)
                .beanName(beanName)
                .build();
        event.commit();
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        var contextId = applicationContext.getId();
        var event = PostProcessAfterInitializationEvent.builder()
                .contextId(contextId)
                .beanName(beanName)
                .build();
        event.commit();
        return bean;
    }
}
