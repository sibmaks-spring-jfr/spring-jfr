package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.core.converter.DependencyConverter;
import io.github.sibmaks.spring.jfr.event.publish.bean.BeanDefinitionRegisteredEvent;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @author sibmaks
 * @since 0.0.2
 */
public final class JavaFlightRecorderBeanDefinitionEventProducer implements MergedBeanDefinitionPostProcessor {
    private final ContextIdProvider contextIdProvider;
    private final ConfigurableListableBeanFactory beanFactory;

    public JavaFlightRecorderBeanDefinitionEventProducer(
            ContextIdProvider contextIdProvider,
            ConfigurableListableBeanFactory beanFactory
    ) {
        this.contextIdProvider = contextIdProvider;
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (BeanFactoryUtils.isGeneratedBeanName(beanName)) {
            produceGenerated(beanName, beanType);
        } else {
            produce(beanName, beanType);
        }
    }

    private void produce(String beanName, Class<?> beanType) {
        if (!beanFactory.containsBeanDefinition(beanName)) {
            produceGenerated(beanName, beanType);
            return;
        }

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

        var contextId = contextIdProvider.getContextId();
        BeanDefinitionRegisteredEvent.builder()
                .contextId(contextId)
                .scope(scope)
                .beanClassName(beanClassName)
                .beanName(beanName)
                .primary(String.valueOf(beanDefinition.isPrimary()))
                .dependencies(DependencyConverter.convert(dependencies.toArray(String[]::new)))
                .generated(false)
                .build()
                .commit();
    }

    private void produceGenerated(String beanName, Class<?> beanType) {
        var dependencies = Optional.of(beanFactory.getDependenciesForBean(beanName))
                .map(List::of)
                .map(HashSet::new)
                .orElseGet(HashSet::new);

        var contextId = contextIdProvider.getContextId();
        var beanClassName = beanType.getCanonicalName();

        var event = BeanDefinitionRegisteredEvent.builder()
                .contextId(contextId)
                .beanClassName(beanClassName)
                .beanName(beanName)
                .dependencies(DependencyConverter.convert(dependencies.toArray(String[]::new)))
                .generated(true)
                .build();
        event.commit();
    }
}
