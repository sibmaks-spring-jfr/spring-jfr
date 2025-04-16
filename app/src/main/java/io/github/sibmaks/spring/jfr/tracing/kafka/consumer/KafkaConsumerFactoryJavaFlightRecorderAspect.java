package io.github.sibmaks.spring.jfr.tracing.kafka.consumer;

import io.github.sibmaks.spring.jfr.event.core.converter.ArrayConverter;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.KafkaConsumerCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.kafka.core.ConsumerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;


/**
 * @author sibmaks
 * @since 0.0.27
 */
@Slf4j
@Aspect
public class KafkaConsumerFactoryJavaFlightRecorderAspect {
    private final String contextId;

    public KafkaConsumerFactoryJavaFlightRecorderAspect(
            String contextId
    ) {
        this.contextId = contextId;
    }

    @Around("execution(* org.springframework.kafka.core.ConsumerFactory.createConsumer(..))")
    public Object wrapConsumer(ProceedingJoinPoint pjp) throws Throwable {
        var consumer = (Consumer<?, ?>) pjp.proceed(pjp.getArgs());

        var consumerFactory = (ConsumerFactory<?, ?>) pjp.getTarget();
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
                .consumerId(consumerId)
                .bootstrapServers(String.valueOf(bootstrapServers))
                .consumerGroup(consumerGroupMetadata.groupId())
                .topics(ArrayConverter.convert(subscription))
                .build()
                .commit();

        return aopConsumer;
    }
}
