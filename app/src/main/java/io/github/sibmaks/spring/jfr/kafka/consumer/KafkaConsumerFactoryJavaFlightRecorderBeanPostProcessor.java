package io.github.sibmaks.spring.jfr.kafka.consumer;

import io.github.sibmaks.spring.jfr.Internal;
import io.github.sibmaks.spring.jfr.JavaFlightRecorderObjectRegistry;
import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.kafka.consumer.rebalance.AspectConsumerRebalanceListener;
import io.github.sibmaks.spring.jfr.kafka.consumer.rebalance.SimpleConsumerRebalanceListener;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

/**
 * @author sibmaks
 * @since 0.0.27
 */
@Internal
public class KafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor implements BeanPostProcessor {
    private final String contextId;
    private final JavaFlightRecorderObjectRegistry objectRegistry;

    public KafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor(
            ContextIdProvider contextIdProvider,
            JavaFlightRecorderObjectRegistry objectRegistry
    ) {
        this.contextId = contextIdProvider.getContextId();
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
