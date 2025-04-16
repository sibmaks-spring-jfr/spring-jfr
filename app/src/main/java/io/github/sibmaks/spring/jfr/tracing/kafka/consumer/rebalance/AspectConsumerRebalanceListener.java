package io.github.sibmaks.spring.jfr.tracing.kafka.consumer.rebalance;

import io.github.sibmaks.spring.jfr.JavaFlightRecorderObjectRegistry;
import io.github.sibmaks.spring.jfr.event.core.converter.ArrayConverter;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.topic.partition.KafkaConsumerPartitionAssignedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.topic.partition.KafkaConsumerPartitionLostEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.topic.partition.KafkaConsumerPartitionRevokedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

import java.util.Collection;

@Aspect
@RequiredArgsConstructor
public class AspectConsumerRebalanceListener {
    private final JavaFlightRecorderObjectRegistry objectRegistry;

    private static String getPartitions(Collection<TopicPartition> partitions) {
        return ArrayConverter.convert(
                partitions
                        .stream()
                        .map(it -> String.format("%s-%d", it.topic(), it.partition()))
                        .toArray(String[]::new)
        );
    }

    @After(
            value = "execution(* org.springframework.kafka.listener.ConsumerAwareRebalanceListener.onPartitionsRevokedAfterCommit(..)) && " +
                    "args(consumer, partitions)",
            argNames = "consumer,partitions"
    )
    public void onPartitionsRevokedAfterCommit(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        var consumerId = objectRegistry.registerObject(consumer);

        KafkaConsumerPartitionRevokedEvent.builder()
                .consumerId(consumerId)
                .partitions(getPartitions(partitions))
                .build()
                .commit();
    }

    @After(
            value = "execution(* org.springframework.kafka.listener.ConsumerAwareRebalanceListener.onPartitionsRevokedBeforeCommit(..)) && " +
                    "args(consumer, partitions)",
            argNames = "consumer,partitions"
    )
    public void onPartitionsRevokedBeforeCommit(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        var consumerId = objectRegistry.registerObject(consumer);

        KafkaConsumerPartitionRevokedEvent.builder()
                .consumerId(consumerId)
                .partitions(getPartitions(partitions))
                .build()
                .commit();
    }

    @After(
            value = "execution(* org.springframework.kafka.listener.ConsumerAwareRebalanceListener.onPartitionsLost(..)) && " +
                    "args(consumer, partitions)",
            argNames = "consumer,partitions"
    )
    public void onPartitionsLost(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        var consumerId = objectRegistry.registerObject(consumer);

        KafkaConsumerPartitionLostEvent.builder()
                .consumerId(consumerId)
                .partitions(getPartitions(partitions))
                .build()
                .commit();
    }

    @After(
            value = "execution(* org.springframework.kafka.listener.ConsumerAwareRebalanceListener.onPartitionsAssigned(..)) && " +
                    "args(consumer, partitions)",
            argNames = "consumer,partitions"
    )
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        var consumerId = objectRegistry.registerObject(consumer);

        KafkaConsumerPartitionAssignedEvent.builder()
                .consumerId(consumerId)
                .partitions(getPartitions(partitions))
                .build()
                .commit();
    }


}
