package io.github.sibmaks.spring.jfr.event.bean;

import io.github.sibmaks.spring.jfr.event.SpringJavaFlightRecorderEvent;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@StackTrace(false)
@Name("io.github.sibmaks.spring.jfr.event.bean.PostProcessBeforeInitializationEvent")
@Label("Post Process Before Initialization Invoked")
public class PostProcessBeforeInitializationEvent extends SpringJavaFlightRecorderEvent {
    @Label("Bean name")
    private final String beanName;

    public PostProcessBeforeInitializationEvent(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}
