package io.github.sibmaks.spring.jfr.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author sibmaks
 * @since 0.0.9
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InvocationContext {
    public static final ThreadLocal<List<String>> TRACE_THREAD_LOCAL = ThreadLocal.withInitial(ArrayList::new);

    /**
     * Get last trace id in current thread
     *
     * @return thread trace id
     */
    public static String getTraceId() {
        var trace = TRACE_THREAD_LOCAL.get();
        if (trace.isEmpty()) {
            return null;
        }
        return trace.get(trace.size() - 1);
    }

    /**
     * Generate trace id and start trace
     *
     * @return trace identifier
     */
    public static String startTrace() {
        var traceId = UUID.randomUUID().toString();
        startTrace(traceId);
        return traceId;
    }

    /**
     * Start trace with passed identifier
     *
     * @param traceId trace identifier
     */
    public static void startTrace(String traceId) {
        var trace = TRACE_THREAD_LOCAL.get();
        trace.add(traceId);
    }

    /**
     * Stop trace with specific trace identifier
     *
     * @param traceId trace identifier to remove
     */
    public static void stopTrace(String traceId) {
        var trace = TRACE_THREAD_LOCAL.get();
        if (trace.isEmpty()) {
            return;
        }
        var lastTrace = trace.get(trace.size() - 1);
        if (lastTrace.equals(traceId)) {
            trace.remove(trace.size() - 1);
            return;
        }
        var index = trace.indexOf(traceId);
        if (index != -1) {
            trace.subList(index, trace.size()).clear();
        }
    }
}
