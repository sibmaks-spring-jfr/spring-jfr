package io.github.sibmaks.spring.jfr.jpa;

import io.github.sibmaks.spring.jfr.event.jpa.JpaEventInvocationEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class JpaRepositoryJavaFlightRecorderAspect {

    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void jpaRepositoryMethods() {
    }

    @Around("jpaRepositoryMethods()")
    public Object traceJpaRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        var event = new JpaEventInvocationEvent(
                joinPoint.getSignature().toString()
        );

        event.begin();
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            event.setException(throwable.toString());
            throw throwable;
        } finally {
            event.commit();
        }
    }

}
