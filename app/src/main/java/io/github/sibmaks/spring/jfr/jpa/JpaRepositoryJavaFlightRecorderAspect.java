package io.github.sibmaks.spring.jfr.jpa;

import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.jpa.JPAMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.jpa.JPAMethodFailedEvent;
import io.github.sibmaks.spring.jfr.event.jpa.JPAMethodInvokedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.UUID;

@Aspect
public class JpaRepositoryJavaFlightRecorderAspect {

    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void jpaRepositoryMethods() {
    }

    @Around("jpaRepositoryMethods()")
    public Object traceJpaRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        var correlationId = InvocationContext.getTraceId();
        var invocationId = UUID.randomUUID().toString();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        var event = JPAMethodInvokedEvent.builder()
                .correlationId(correlationId)
                .invocationId(invocationId)
                .className(methodSignature.getDeclaringType().getCanonicalName())
                .methodName(methodSignature.getName())
                .build();
        event.commit();

        try {
            var result = joinPoint.proceed();

            var finishedEvent = JPAMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build();
            finishedEvent.commit();

            return result;
        } catch (Throwable throwable) {
            var failEvent = JPAMethodFailedEvent.builder()
                    .invocationId(invocationId)
                    .exceptionClass(throwable.getClass().getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build();
            failEvent.commit();

            throw throwable;
        }
    }

}
