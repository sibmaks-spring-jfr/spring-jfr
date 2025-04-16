package io.github.sibmaks.spring.jfr.core.impl;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author sibmaks
 * @since 0.0.9
 */
public class ContextIdProviderImpl implements ContextIdProvider {
    private final ApplicationContext applicationContext;

    public ContextIdProviderImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public String getContextId() {
        var parts = new ArrayList<String>();
        parts.add(applicationContext.getId());
        parts.add(applicationContext.getDisplayName());
        parts.add(String.valueOf(applicationContext.hashCode()));
        return parts.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining("-"));
    }
}
