package io.github.sibmaks.spring.jfr;

import jdk.jfr.FlightRecorder;
import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sibmaks
 * @since 0.0.27
 */
public class JavaFlightRecorderRecordCounter implements FlightRecorderListener {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void recorderInitialized(FlightRecorder recorder) {
        var recordings = recorder.getRecordings();
        for (var recording : recordings) {
            if (recording.getState() == RecordingState.RUNNING) {
                counter.incrementAndGet();
            }
        }
    }

    @Override
    public void recordingStateChanged(Recording recording) {
        switch (recording.getState()) {
            case RUNNING: {
                counter.incrementAndGet();
                break;
            }
            case STOPPED: {
                counter.decrementAndGet();
                break;
            }
        }
    }

    /**
     * Check is any recording is running
     *
     * @return true - some recording is running, false - otherwise
     */
    public boolean hasRunningRecording() {
        return counter.get() > 0;
    }
}
