package io.github.sibmaks.spring.jfr.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author sibmaks
 * @since 0.0.9
 */
public final class InvocationContext {
    public static final ThreadLocal<List<String>> TRACE_THREAD_LOCAL = ThreadLocal.withInitial(ArrayList::new);

    private InvocationContext() {

    }

    public static String getTraceId() {
        var trace = TRACE_THREAD_LOCAL.get();
        if (trace.isEmpty()) {
            return null;
        }
        return trace.get(trace.size() - 1);
    }

    public static String startTrace() {
        var traceId = UUID.randomUUID().toString();
        startTrace(traceId);
        return traceId;
    }

    public static void startTrace(String traceId) {
        var trace = TRACE_THREAD_LOCAL.get();
        trace.add(traceId);
    }

    public static void stopTrace(String invocationId) {
        var trace = TRACE_THREAD_LOCAL.get();
        var index = trace.indexOf(invocationId);
        if (index != -1) {
            trace.subList(index, trace.size()).clear();
        }
    }
}
