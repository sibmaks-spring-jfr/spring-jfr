package com.github.sibmaks.spring.jfr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class JavaFlightRecorderCondition implements Condition {
    private static final Logger log = LoggerFactory.getLogger(JavaFlightRecorderCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var attributes = metadata.getAnnotationAttributes(JavaFlightRecorderConditional.class.getName());
        if (attributes == null) {
            return false;
        }

        var properties = (AnnotationAttributes[]) attributes.get("properties");
        var environment = context.getEnvironment();
        for (var property : properties) {
            var key = (String) property.get("key");

            var matchIfMissing = (boolean) property.get("matchIfMissing");
            if (matchIfMissing && !environment.containsProperty(key)) {
                continue;
            }

            var actual = environment.getProperty(key);
            var value = (String) property.get("value");
            if (!Objects.equals(value, actual)) {
                return false;
            }
        }

        var requiredClasses = attributes.get("requiredClasses");
        if (!(requiredClasses instanceof String[] classes)) {
            return false;
        }
        var classLoader = context.getClassLoader();
        try {
            for (var type : classes) {
                classLoader.loadClass(type);
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
