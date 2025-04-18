package io.github.sibmaks.spring.jfr.tracing.kafka.consumer;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderObjectRegistry;
import io.github.sibmaks.spring.jfr.tracing.kafka.consumer.rebalance.AspectConsumerRebalanceListener;
import io.github.sibmaks.spring.jfr.tracing.kafka.consumer.rebalance.SimpleConsumerRebalanceListener;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

/**
 * @author sibmaks
 * @since 0.0.27
 */
public class KafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor implements BeanPostProcessor {
    private final String contextId;
    private final JavaFlightRecorderObjectRegistry objectRegistry;

    public KafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor(
            String contextId,
            JavaFlightRecorderObjectRegistry objectRegistry
    ) {
        this.contextId = contextId;
        this.objectRegistry = objectRegistry;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ConsumerFactory) {
            var consumerFactory = (ConsumerFactory) bean;
            var postProcessor = new KafkaConsumerFactoryListener<>(contextId, beanName, objectRegistry, consumerFactory);
            consumerFactory.addPostProcessor(postProcessor);
        }
        if (bean instanceof AbstractKafkaListenerContainerFactory) {
            var factory = (AbstractKafkaListenerContainerFactory<?, ?, ?>) bean;
            var containerProperties = factory.getContainerProperties();
            var consumerRebalanceListener = containerProperties.getConsumerRebalanceListener();
            if (consumerRebalanceListener != null) {
                var proxyFactory = new AspectJProxyFactory(consumerRebalanceListener);
                proxyFactory.addAspect(new AspectConsumerRebalanceListener(objectRegistry));
                containerProperties.setConsumerRebalanceListener(proxyFactory.getProxy());
            } else {
                containerProperties.setConsumerRebalanceListener(new SimpleConsumerRebalanceListener(objectRegistry));
            }
        }
        return bean;
    }
}
