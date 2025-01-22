package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.event.api.bean.Stereotype;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author sibmaks
 * @since 0.0.12
 */
public final class BeanDefinitions {

    private BeanDefinitions() {

    }

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
        if (isAnnotationPresent(beanType, "org.springframework.web.bind.annotation.RestController")) {
            return Stereotype.REST_CONTROLLER;
        }
        if (isAnnotationPresent(beanType, "org.springframework.stereotype.Controller")) {
            return Stereotype.CONTROLLER;
        }
        if (isAnnotationPresent(beanType, "org.springframework.stereotype.Service")) {
            return Stereotype.SERVICE;
        }
        if (isAnnotationPresent(beanType, "org.springframework.stereotype.Repository")) {
            return Stereotype.REPOSITORY;
        }
        if (isAnnotationPresent(beanType, "org.springframework.stereotype.Component")) {
            return Stereotype.COMPONENT;
        }
        return Stereotype.UNKNOWN;
    }

    private static boolean isAnnotationPresent(Class<?> type, String annotationClassName) {
        try {
            var annotationType = Class.forName(annotationClassName);
            return type.isAnnotationPresent((Class<? extends Annotation>) annotationType);
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

    /**
     * Get bean type
     *
     * @param beanDefinition bean definition
     * @param defaultType    default bean type
     * @return bean type
     */
    public static Class<?> getBeanType(BeanDefinition beanDefinition, Class<?> defaultType) {
        return Optional.ofNullable(beanDefinition.getBeanClassName())
                .flatMap(it -> {
                    try {
                        return Optional.of(Class.forName(it));
                    } catch (ClassNotFoundException e) {
                        return Optional.<Class<?>>empty();
                    }
                })
                .orElse(defaultType);
    }
}
