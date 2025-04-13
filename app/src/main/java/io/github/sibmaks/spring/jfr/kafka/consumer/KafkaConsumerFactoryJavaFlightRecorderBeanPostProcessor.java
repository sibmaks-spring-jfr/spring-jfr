package io.github.sibmaks.spring.jfr.kafka.consumer;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.core.ConsumerFactory;

/**
 * @author sibmaks
 * @since 0.0.27
 */
public class KafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor implements BeanPostProcessor {
    private final String contextId;

    public KafkaConsumerFactoryJavaFlightRecorderBeanPostProcessor(ContextIdProvider contextIdProvider) {
        this.contextId = contextIdProvider.getContextId();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ConsumerFactory) {
            var consumerFactory = (ConsumerFactory) bean;
            var postProcessor = new KafkaConsumerFactoryListener<>(contextId, beanName, consumerFactory);
            consumerFactory.addPostProcessor(postProcessor);
        }
        return bean;
    }
}
