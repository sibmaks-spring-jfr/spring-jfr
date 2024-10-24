package io.github.sibmaks.spring.jfr.scheduler;

import io.github.sibmaks.spring.jfr.event.scheduled.ScheduledInvocationEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.annotation.Scheduled;

@Aspect
public class SchedulerJavaFlightRecorderAspect {

    @Pointcut("@annotation(scheduled)")
    public void scheduledMethods(Scheduled scheduled) {
    }

    @Around(value = "scheduledMethods(scheduled)", argNames = "joinPoint,scheduled")
    public Object traceScheduledMethods(ProceedingJoinPoint joinPoint, Scheduled scheduled) throws Throwable {
        var scheduledEvent = ScheduledInvocationEvent.builder()
                .methodName(joinPoint.getSignature().toString())
                .build();
        scheduledEvent.begin();
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            scheduledEvent.setException(throwable.toString());
            throw throwable;
        } finally {
            scheduledEvent.commit();
        }
    }
}
