package io.github.sibmaks.spring.jfr.event.bean;

import io.github.sibmaks.spring.jfr.event.SpringJavaFlightRecorderEvent;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@StackTrace(false)
@Name("io.github.sibmaks.spring.jfr.event.bean.PostProcessAfterInitializationEvent")
@Label("Post Process After Initialization Invoked")
public class PostProcessAfterInitializationEvent extends SpringJavaFlightRecorderEvent {
    @Label("Bean name")
    private final String beanName;

    public PostProcessAfterInitializationEvent(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}
