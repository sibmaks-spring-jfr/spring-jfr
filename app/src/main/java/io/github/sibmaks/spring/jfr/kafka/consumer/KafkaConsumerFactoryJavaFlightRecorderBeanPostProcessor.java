package io.github.sibmaks.spring.jfr.kafka.consumer;

import io.github.sibmaks.spring.jfr.Internal;
import io.github.sibmaks.spring.jfr.JavaFlightRecorderObjectRegistry;
import io.github.sibmaks.spring.jfr.kafka.consumer.rebalance.AspectConsumerRebalanceListener;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory;

/**
 * @author sibmaks
 * @since 0.0.27
 */
@Internal
public class KafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor implements BeanPostProcessor {
    private final JavaFlightRecorderObjectRegistry objectRegistry;

    public KafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor(
            JavaFlightRecorderObjectRegistry objectRegistry
    ) {
        this.objectRegistry = objectRegistry;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AbstractKafkaListenerContainerFactory) {
            var factory = (AbstractKafkaListenerContainerFactory<?, ?, ?>) bean;
            var containerProperties = factory.getContainerProperties();
            var consumerRebalanceListener = containerProperties.getConsumerRebalanceListener();
            if (consumerRebalanceListener != null) {
                var proxyFactory = new AspectJProxyFactory(consumerRebalanceListener);
                proxyFactory.addAspect(new AspectConsumerRebalanceListener(objectRegistry));
                containerProperties.setConsumerRebalanceListener(proxyFactory.getProxy());
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
