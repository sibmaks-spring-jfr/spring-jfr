package io.github.sibmaks.spring.jfr.jpa;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.core.InvocationContext;
import io.github.sibmaks.spring.jfr.event.publish.jpa.JPAMethodCalledEvent;
import io.github.sibmaks.spring.jfr.event.publish.jpa.JPAMethodExecutedEvent;
import io.github.sibmaks.spring.jfr.event.publish.jpa.JPAMethodFailedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class JpaRepositoryJavaFlightRecorderAspect {
    private final ContextIdProvider contextIdProvider;

    public JpaRepositoryJavaFlightRecorderAspect(ContextIdProvider contextIdProvider) {
        this.contextIdProvider = contextIdProvider;
    }

    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void jpaRepositoryMethods() {
    }

    @Around("jpaRepositoryMethods()")
    public Object traceJpaRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        var contextId = contextIdProvider.getContextId();
        var correlationId = InvocationContext.getTraceId();
        var invocationId = InvocationContext.startTrace();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;

        JPAMethodCalledEvent.builder()
                .contextId(contextId)
                .correlationId(correlationId)
                .invocationId(invocationId)
                .className(methodSignature.getDeclaringType().getCanonicalName())
                .methodName(methodSignature.getName())
                .build()
                .commit();

        try {
            var args = joinPoint.getArgs();
            var result = joinPoint.proceed(args);

            JPAMethodExecutedEvent.builder()
                    .invocationId(invocationId)
                    .build()
                    .commit();

            return result;
        } catch (Throwable throwable) {
            JPAMethodFailedEvent.builder()
                    .invocationId(invocationId)
                    .exceptionClass(throwable.getClass().getCanonicalName())
                    .exceptionMessage(throwable.getMessage())
                    .build()
                    .commit();

            throw throwable;
        } finally {
            InvocationContext.stopTrace(invocationId);
        }
    }

}
