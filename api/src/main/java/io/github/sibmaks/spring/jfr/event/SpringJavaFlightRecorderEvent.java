package io.github.sibmaks.spring.jfr.event;

import jdk.jfr.Event;
import jdk.jfr.StackTrace;

public abstract class SpringJavaFlightRecorderEvent extends Event {

    /**
     * Begin and commit event
     */
    public void record() {
        begin();
        commit();
    }
}
