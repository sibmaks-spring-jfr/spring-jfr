package io.github.sibmaks.spring.jfr;

import jdk.jfr.Recording;

/**
 * @author sibmaks
 * @since 0.0.27
 */
public interface JavaFlightRecordingListener {

    void onRunning(Recording recording);

}
