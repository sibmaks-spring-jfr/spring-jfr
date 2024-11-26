package io.github.sibmaks.spring.jfr.core;

/**
 * @author sibmaks
 * @since 0.0.9
 */
@FunctionalInterface
public interface ContextIdProvider {

    /**
     * @return context id
     */
    String getContextId();

}
