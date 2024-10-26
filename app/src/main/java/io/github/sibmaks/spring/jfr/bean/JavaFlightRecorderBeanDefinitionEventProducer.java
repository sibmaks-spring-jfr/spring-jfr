package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.event.bean.BeanDefinitionRegisteredEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @author sibmaks
 * @since 0.0.2
 */
public final class JavaFlightRecorderBeanDefinitionEventProducer implements BeanPostProcessor {
    private final ConfigurableListableBeanFactory beanFactory;

    public JavaFlightRecorderBeanDefinitionEventProducer(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (BeanFactoryUtils.isGeneratedBeanName(beanName)) {
            produceGenerated(beanName, bean.getClass());
        } else {
            produce(beanName, bean.getClass());
        }
        return bean;
    }

    private void produce(String beanName, Class<?> beanType) {
        var beanDefinition = beanFactory.getMergedBeanDefinition(beanName);

        var dependencies = Optional.ofNullable(beanDefinition.getDependsOn())
                .map(List::of)
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        var otherDeps = Optional.of(beanFactory.getDependenciesForBean(beanName))
                .map(List::of)
                .map(HashSet::new)
                .orElseGet(HashSet::new);
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
                .generated(false)
                .build();
        event.commit();
    }

    private void produceGenerated(String beanName, Class<?> beanType) {
        var dependencies = Optional.of(beanFactory.getDependenciesForBean(beanName))
                .map(List::of)
                .map(HashSet::new)
                .orElseGet(HashSet::new);

        var beanClassName = beanType.getCanonicalName();

        var event = BeanDefinitionRegisteredEvent.builder()
                .beanClassName(beanClassName)
                .beanName(beanName)
                .dependencies(dependencies.toArray(String[]::new))
                .generated(true)
                .build();
        event.commit();
    }
}
