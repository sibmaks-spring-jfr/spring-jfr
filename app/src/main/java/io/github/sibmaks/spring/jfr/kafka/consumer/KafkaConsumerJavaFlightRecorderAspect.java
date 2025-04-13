package io.github.sibmaks.spring.jfr.kafka.consumer;

import io.github.sibmaks.spring.jfr.event.core.converter.ArrayConverter;
import io.github.sibmaks.spring.jfr.event.recording.kafka.consumer.KafkaConsumerClosedEvent;
import io.github.sibmaks.spring.jfr.event.recording.kafka.consumer.commit.KafkaConsumerCommitEvent;
import io.github.sibmaks.spring.jfr.event.recording.kafka.consumer.commit.KafkaConsumerCommitFailedEvent;
import io.github.sibmaks.spring.jfr.event.recording.kafka.consumer.commit.KafkaConsumerCommitedEvent;
import io.github.sibmaks.spring.jfr.event.recording.kafka.consumer.topic.KafkaConsumerTopicsSubscribedEvent;
import io.github.sibmaks.spring.jfr.event.recording.kafka.consumer.topic.KafkaConsumerTopicsUnsubscribedEvent;
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
        var consumer = (Consumer<?, ?>) pjp.getTarget();
        var was = Optional.ofNullable(consumer.subscription())
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        var result = pjp.proceed(pjp.getArgs());
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

    @Around("execution(* org.apache.kafka.clients.consumer.Consumer.commitSync(java.util.Map<org.apache.kafka.common.TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata>, ..))")
    public Object onCommitSync(ProceedingJoinPoint pjp) throws Throwable {
        return onAnyCommit(pjp, false);
    }

    @Around("execution(* org.apache.kafka.clients.consumer.Consumer.commitAsync(java.util.Map<org.apache.kafka.common.TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata>, ..))")
    public Object onCommitAsync(ProceedingJoinPoint pjp) throws Throwable {
        return onAnyCommit(pjp, true);
    }

    @Around("execution(* org.apache.kafka.clients.consumer.Consumer.close())")
    public Object onClose(ProceedingJoinPoint pjp) throws Throwable {
        KafkaConsumerClosedEvent.builder()
                .consumerId(consumerId)
                .build()
                .commit();
        return pjp.proceed(pjp.getArgs());
    }

    private Object onAnyCommit(ProceedingJoinPoint pjp, boolean async) throws Throwable {
        var commitId = UUID.randomUUID().toString();
        var args = pjp.getArgs();
        var offsets = (Map<TopicPartition, OffsetAndMetadata>) args[0];
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
            KafkaConsumerCommitFailedEvent.builder()
                    .commitId(commitId)
                    .exceptionClass(throwable.getClass().getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build()
                    .commit();
            throw throwable;
        }
    }

}
