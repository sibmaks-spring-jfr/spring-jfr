package io.github.sibmaks.spring.jfr;

import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @author sibmaks
 * @since 0.0.27
 */
@Slf4j
@AllArgsConstructor
public class JavaFlightRecorderListenerAdapter implements FlightRecorderListener, BeanFactoryPostProcessor {
    private final JavaFlightRecordingListener listener;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void recordingStateChanged(Recording recording) {
        switch (recording.getState()) {
            case NEW:
                break;
            case DELAYED:
                break;
            case RUNNING:
                listener.onRunning(recording);
                break;
            case STOPPED:
                break;
            case CLOSED:
                break;
        }
    }
}
