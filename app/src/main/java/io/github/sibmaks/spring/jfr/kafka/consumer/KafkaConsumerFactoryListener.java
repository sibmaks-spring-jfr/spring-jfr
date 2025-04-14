package io.github.sibmaks.spring.jfr.kafka.consumer;

import io.github.sibmaks.spring.jfr.Internal;
import io.github.sibmaks.spring.jfr.JavaFlightRecorderObjectRegistry;
import io.github.sibmaks.spring.jfr.event.core.converter.ArrayConverter;
import io.github.sibmaks.spring.jfr.event.recording.kafka.consumer.KafkaConsumerCreatedEvent;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ConsumerPostProcessor;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * @author sibmaks
 * @since 0.0.27
 */
@Internal
@AllArgsConstructor
public class KafkaConsumerFactoryListener<K, V> implements ConsumerPostProcessor<K, V> {
    private final String contextId;
    private final String consumerFactoryName;
    private final JavaFlightRecorderObjectRegistry javaFlightRecorderObjectRegistry;
    private final ConsumerFactory<K, V> consumerFactory;

    @Override
    public Consumer<K, V> apply(Consumer<K, V> consumer) {
        var configurationProperties = consumerFactory.getConfigurationProperties();
        var bootstrapServers = configurationProperties.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
        var consumerGroupMetadata = consumer.groupMetadata();
        var subscription = Optional.ofNullable(consumer.subscription())
                .orElseGet(Collections::emptySet)
                .toArray(String[]::new);

        var aopConsumer = consumer;

        var consumerId = UUID.randomUUID().toString();
        if (!AopUtils.isAopProxy(aopConsumer)) {
            var proxyFactory = new AspectJProxyFactory(consumer);
            proxyFactory.addAspect(new KafkaConsumerJavaFlightRecorderAspect(consumerId));
            aopConsumer = proxyFactory.getProxy();
        } else {
            //TODO: wrap already aspected
        }

        KafkaConsumerCreatedEvent.builder()
                .contextId(contextId)
                .consumerFactory(consumerFactoryName)
                .consumerId(consumerId)
                .bootstrapServers(String.valueOf(bootstrapServers))
                .consumerGroup(consumerGroupMetadata.groupId())
                .topics(ArrayConverter.convert(subscription))
                .build()
                .commit();

        javaFlightRecorderObjectRegistry.registerObject(
                aopConsumer,
                consumerId
        );

        return aopConsumer;
    }
}
