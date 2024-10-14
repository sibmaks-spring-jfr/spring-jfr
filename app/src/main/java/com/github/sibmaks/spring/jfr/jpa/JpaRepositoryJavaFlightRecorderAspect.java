package com.github.sibmaks.spring.jfr.jpa;

import com.github.sibmaks.spring.jfr.event.JpaEventInvocationEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class JpaRepositoryJavaFlightRecorderAspect {

    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void jpaRepositoryMethods() {
        // Pointcut to capture all methods in classes that implement JpaRepository
    }

    @Around("jpaRepositoryMethods()")
    public Object traceJpaRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        var event = new JpaEventInvocationEvent(joinPoint.getSignature().toShortString());

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
