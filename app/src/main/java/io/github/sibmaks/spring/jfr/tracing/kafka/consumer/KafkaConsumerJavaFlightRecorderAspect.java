package io.github.sibmaks.spring.jfr.tracing.kafka.consumer;

import io.github.sibmaks.spring.jfr.event.core.converter.ArrayConverter;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.KafkaConsumerClosedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.commit.KafkaConsumerCommitEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.commit.KafkaConsumerCommitFailedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.commit.KafkaConsumerCommitedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.topic.KafkaConsumerTopicsSubscribedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.topic.KafkaConsumerTopicsUnsubscribedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Aspect
public class KafkaConsumerJavaFlightRecorderAspect {
    private final String consumerId;

    public KafkaConsumerJavaFlightRecorderAspect(
            String consumerId
    ) {
        this.consumerId = consumerId;
    }

    @Around("execution(* org.apache.kafka.clients.consumer.Consumer.subscribe(..))")
    public Object onSubscribe(ProceedingJoinPoint pjp) throws Throwable {
        var args = pjp.getArgs();
        if (args.length < 2) {
            return pjp.proceed(args);
        }
        var consumer = (Consumer<?, ?>) pjp.getTarget();
        var was = Optional.ofNullable(consumer.subscription())
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        var result = pjp.proceed(args);
        var is = Optional.ofNullable(consumer.subscription())
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        is.removeAll(was);

        if (!is.isEmpty()) {
            KafkaConsumerTopicsSubscribedEvent.builder()
                    .consumerId(consumerId)
                    .topics(ArrayConverter.convert(is.toArray(new String[0])))
                    .build()
                    .commit();
        }

        return result;
    }

    @Around("execution(* org.apache.kafka.clients.consumer.Consumer.unsubscribe(..))")
    public Object onUnsubscribe(ProceedingJoinPoint pjp) throws Throwable {
        var consumer = (Consumer<?, ?>) pjp.getTarget();
        var was = Optional.ofNullable(consumer.subscription())
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        var result = pjp.proceed(pjp.getArgs());
        KafkaConsumerTopicsUnsubscribedEvent.builder()
                .consumerId(consumerId)
                .topics(ArrayConverter.convert(was.toArray(new String[0])))
                .build()
                .commit();
        return result;
    }

    @Around("execution(* org.apache.kafka.clients.consumer.Consumer.commitSync(java.util.Map<org.apache.kafka.common.TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata>, ..)) && " +
            "args(offsets, ..)")
    public Object onCommitSync(
            ProceedingJoinPoint pjp,
            Map<TopicPartition, OffsetAndMetadata> offsets
    ) throws Throwable {
        return onAnyCommit(pjp, offsets, false);
    }

    @Around("execution(* org.apache.kafka.clients.consumer.Consumer.commitAsync(java.util.Map<org.apache.kafka.common.TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata>, ..)) && " +
            "args(offsets, ..)")
    public Object onCommitAsync(
            ProceedingJoinPoint pjp,
            Map<TopicPartition, OffsetAndMetadata> offsets
    ) throws Throwable {
        return onAnyCommit(pjp, offsets, true);
    }

    @Around("execution(* org.apache.kafka.clients.consumer.Consumer.close())")
    public Object onClose(ProceedingJoinPoint pjp) throws Throwable {
        KafkaConsumerClosedEvent.builder()
                .consumerId(consumerId)
                .build()
                .commit();
        return pjp.proceed(pjp.getArgs());
    }

    private Object onAnyCommit(
            ProceedingJoinPoint pjp,
            Map<TopicPartition, OffsetAndMetadata> offsets,
            boolean async
    ) throws Throwable {
        var commitId = UUID.randomUUID().toString();
        var offsetArray = new String[offsets.size() * 2];
        var offset = 0;
        for (var entry : offsets.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            offsetArray[offset++] = key.topic() + '-' + key.partition();
            offsetArray[offset++] = String.valueOf(value.offset());
        }

        KafkaConsumerCommitEvent.builder()
                .consumerId(consumerId)
                .commitId(commitId)
                .async(async)
                .offsets(ArrayConverter.convert(offsetArray))
                .build()
                .commit();

        try {
            var result = pjp.proceed(pjp.getArgs());
            KafkaConsumerCommitedEvent.builder()
                    .commitId(commitId)
                    .build()
                    .commit();
            return result;
        } catch (Throwable throwable) {
            var throwableClass = throwable.getClass();
            KafkaConsumerCommitFailedEvent.builder()
                    .commitId(commitId)
                    .exceptionClass(throwableClass.getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build()
                    .commit();
            throw throwable;
        }
    }

}
