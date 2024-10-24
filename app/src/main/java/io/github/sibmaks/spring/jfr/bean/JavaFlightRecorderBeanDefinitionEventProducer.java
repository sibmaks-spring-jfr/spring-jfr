package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.event.bean.BeanDefinitionRegisteredEvent;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @author sibmaks
 * @since 0.0.2
 */
public final class JavaFlightRecorderBeanDefinitionEventProducer {
    private final ConfigurableListableBeanFactory beanFactory;

    public JavaFlightRecorderBeanDefinitionEventProducer(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void produce(String beanName, Class<?> beanType) {
        var beanDefinition = beanFactory.getBeanDefinition(beanName);
        var dependsOn = Optional.ofNullable(beanDefinition.getDependsOn())
                .map(List::of)
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        var otherDeps = Optional.of(beanFactory.getDependenciesForBean(beanName))
                .map(List::of)
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        var dependencies = new HashSet<>(dependsOn);
        dependencies.addAll(otherDeps);

        var scope = Optional.ofNullable(beanDefinition.getScope())
                .or(() -> Optional.ofNullable(beanDefinition.isSingleton() ? BeanDefinition.SCOPE_SINGLETON : null))
                .or(() -> Optional.ofNullable(beanDefinition.isPrototype() ? BeanDefinition.SCOPE_PROTOTYPE : null))
                .orElse("");

        var beanClassName = Optional.ofNullable(beanDefinition.getBeanClassName())
                .orElse(beanType.getCanonicalName());

        var event = BeanDefinitionRegisteredEvent.builder()
                .scope(scope)
                .beanClassName(beanClassName)
                .beanName(beanName)
                .primary(beanDefinition.isPrimary())
                .dependencies(dependencies.toArray(String[]::new))
                .build();
        event.commit();
    }
}
