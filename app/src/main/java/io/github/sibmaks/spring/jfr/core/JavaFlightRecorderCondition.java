package io.github.sibmaks.spring.jfr.core;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class JavaFlightRecorderCondition implements Condition {

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
        if (!(requiredClasses instanceof String[])) {
            return false;
        }
        var classes = (String[]) requiredClasses;
        var classLoader = context.getClassLoader();
        if (classLoader == null) {
            classLoader = JavaFlightRecorderCondition.class.getClassLoader();
        }
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
