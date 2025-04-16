package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.event.api.bean.Stereotype;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

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
        if (AnnotatedElementUtils.hasAnnotation(beanType, RestController.class)) {
            return Stereotype.REST_CONTROLLER;
        }
        if (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class)) {
            return Stereotype.CONTROLLER;
        }
        if (AnnotatedElementUtils.hasAnnotation(beanType, Service.class)) {
            return Stereotype.SERVICE;
        }
        if (AnnotatedElementUtils.hasAnnotation(beanType, Repository.class)) {
            return Stereotype.REPOSITORY;
        }
        if (org.springframework.data.repository.Repository.class.isAssignableFrom(beanType)) {
            return Stereotype.REPOSITORY;
        }
        if (AnnotatedElementUtils.hasAnnotation(beanType, Component.class)) {
            return Stereotype.COMPONENT;
        }
        return Stereotype.UNKNOWN;
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
