package com.github.sibmaks.spring.jfr;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnClassCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var annotationAttributes = metadata.getAnnotationAttributes(OnClassConditional.class.getName());
        if(annotationAttributes == null) {
            return false;
        }
        var requiredClassName = annotationAttributes.get("value");
        if(!(requiredClassName instanceof String className)) {
            return false;
        }
        var classLoader = context.getClassLoader();
        try {
            classLoader.loadClass(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
