package io.github.sibmaks.spring.jfr.tracing.kafka.consumer;

import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.message.KafkaConsumerMessageFailedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.message.KafkaConsumerMessageProcessedEvent;
import io.github.sibmaks.spring.jfr.event.recording.tracing.kafka.consumer.message.KafkaConsumerMessageReceivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;


/**
 * @author sibmaks
 * @since 0.0.27
 */
@Slf4j
@Aspect
public class KafkaConsumerListenerJavaFlightRecorderAspect {
    private final String contextId;

    public KafkaConsumerListenerJavaFlightRecorderAspect(
            String contextId
    ) {
        this.contextId = contextId;
    }

    @Pointcut("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public void kafkaListenerMethods() {
    }

    @Around(value = "kafkaListenerMethods()", argNames = "joinPoint")
    public Object traceKafkaListenerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        var messageId = InvocationContext.startTrace();

        KafkaConsumerMessageReceivedEvent.builder()
                .messageId(messageId)
                .build()
                .commit();

        try {
            var result = joinPoint.proceed();

            KafkaConsumerMessageProcessedEvent.builder()
                    .messageId(messageId)
                    .build()
                    .commit();

            return result;
        } catch (Throwable throwable) {
            var throwableClass = throwable.getClass();
            KafkaConsumerMessageFailedEvent.builder()
                    .messageId(messageId)
                    .exceptionClass(throwableClass.getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build()
                    .commit();

            throw throwable;
        } finally {
            InvocationContext.stopTrace(messageId);
        }
    }
}
