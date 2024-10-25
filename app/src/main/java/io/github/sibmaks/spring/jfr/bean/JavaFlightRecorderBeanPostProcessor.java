package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.event.bean.PostProcessAfterInitializationEvent;
import io.github.sibmaks.spring.jfr.event.bean.PostProcessBeforeInitializationEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class JavaFlightRecorderBeanPostProcessor implements BeanPostProcessor {
    private final JavaFlightRecorderBeanDefinitionEventProducer eventProducer;

    public JavaFlightRecorderBeanPostProcessor(ConfigurableListableBeanFactory beanFactory) {
        this.eventProducer = new JavaFlightRecorderBeanDefinitionEventProducer(beanFactory);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        var event = new PostProcessBeforeInitializationEvent(beanName);
        event.commit();
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        eventProducer.produce(beanName, bean.getClass());
        var event = new PostProcessAfterInitializationEvent(beanName);
        event.commit();
        return bean;
    }
}
