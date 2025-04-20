package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.event.api.bean.Stereotype;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author sibmaks
 * @since 0.0.12
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BeanDefinitions {

    /**
     * Get stereotype from bean type
     *
     * @param beanType bean type
     * @return stereotype
     */
    public static Stereotype getStereotype(Class<?> beanType) {
        if (beanType == null) {
            return Stereotype.UNKNOWN;
        }
        if (hasAnnotation(beanType, "org.springframework.web.bind.annotation.RestController")) {
            return Stereotype.REST_CONTROLLER;
        }
        if (hasAnnotation(beanType, "org.springframework.stereotype.Controller")) {
            return Stereotype.CONTROLLER;
        }
        if (hasAnnotation(beanType, "org.springframework.stereotype.Service")) {
            return Stereotype.SERVICE;
        }
        if (hasAnnotation(beanType, "org.springframework.stereotype.Repository")) {
            return Stereotype.REPOSITORY;
        }
        if (isAssignableFrom("org.springframework.data.repository.Repository", beanType)) {
            return Stereotype.REPOSITORY;
        }
        if (hasAnnotation(beanType, "org.springframework.stereotype.Component")) {
            return Stereotype.COMPONENT;
        }
        return Stereotype.UNKNOWN;
    }

    /**
     * Determine if an annotation of the specified {@code annotationTypeName}
     * is <em>available</em> on the supplied {@link AnnotatedElement} or
     * within the annotation hierarchy <em>above</em> the specified element.
     *
     * <p>If the annotation class is not present at runtime, then assume that the class being checked does not have the specified annotation.</p>
     *
     * <p>This method follows <em>find semantics</em> as described in the
     * {@linkplain AnnotatedElementUtils class-level javadoc}.
     *
     * @param element            the annotated element
     * @param annotationTypeName the annotation type name to find
     * @return {@code true} if a matching annotation is present
     */
    public static boolean hasAnnotation(AnnotatedElement element, String annotationTypeName) {
        try {
            var annotationType = (Class<? extends Annotation>) Class.forName(annotationTypeName);
            return AnnotatedElementUtils.hasAnnotation(element, annotationType);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Check is class name exists in runtime and assignable from {@code beanType}.
     *
     * @param className class name
     * @param beanType  bean type
     * @return true - assignable, false - otherwise
     */
    public static boolean isAssignableFrom(String className, Class<?> beanType) {
        try {
            var toClassName = Class.forName(className);
            return toClassName.isAssignableFrom(beanType);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Get dependencies from bean definition
     *
     * @param beanFactory    bean factory
     * @param beanName       bean name
     * @param beanDefinition bean definition
     * @return dependencies
     */
    public static Set<String> getDependencies(
            ConfigurableListableBeanFactory beanFactory,
            String beanName,
            BeanDefinition beanDefinition
    ) {
        var dependencies = Optional.ofNullable(beanDefinition.getDependsOn())
                .map(List::of)
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        var otherDeps = Optional.of(beanFactory.getDependenciesForBean(beanName))
                .map(List::of)
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        dependencies.addAll(otherDeps);
        return dependencies;
    }

    /**
     * Get scope
     *
     * @param beanDefinition bean definition
     * @return dependencies
     */
    public static String getScope(BeanDefinition beanDefinition) {
        return Optional.ofNullable(beanDefinition.getScope())
                .or(() -> Optional.ofNullable(beanDefinition.isSingleton() ? BeanDefinition.SCOPE_SINGLETON : null))
                .or(() -> Optional.ofNullable(beanDefinition.isPrototype() ? BeanDefinition.SCOPE_PROTOTYPE : null))
                .orElse("");
    }

}
