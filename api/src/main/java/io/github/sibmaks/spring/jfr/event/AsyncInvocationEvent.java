package io.github.sibmaks.spring.jfr.event;

import jdk.jfr.Event;
import jdk.jfr.Label;

@Label("Async Invocation Event")
public class AsyncInvocationEvent extends Event {
    @Label("Method Name")
    private final String methodName;
    @Label("Feature Execution Time")
    private Long featureExecutionTime;
    @Label("Execution Exception")
    private String executionException;
    @Label("Exception")
    private String exception;

    public AsyncInvocationEvent(
            String methodName
    ) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Long getFeatureExecutionTime() {
        return featureExecutionTime;
    }

    public void setFeatureExecutionTime(Long featureExecutionTime) {
        this.featureExecutionTime = featureExecutionTime;
    }

    public String getExecutionException() {
        return executionException;
    }

    public void setExecutionException(String executionException) {
        this.executionException = executionException;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
