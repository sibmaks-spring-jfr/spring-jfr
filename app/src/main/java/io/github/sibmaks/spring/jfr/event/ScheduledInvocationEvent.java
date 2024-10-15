package io.github.sibmaks.spring.jfr.event;

import jdk.jfr.Event;
import jdk.jfr.Label;

@Label("Scheduled Task Invocation Event")
public class ScheduledInvocationEvent extends Event {
    @Label("Method Name")
    private final String methodName;

    @Label("Exception")
    private String exception;

    public ScheduledInvocationEvent(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
