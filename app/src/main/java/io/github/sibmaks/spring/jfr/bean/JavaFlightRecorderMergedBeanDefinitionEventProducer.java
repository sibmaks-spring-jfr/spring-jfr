package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.core.converter.DependencyConverter;
import io.github.sibmaks.spring.jfr.event.publish.bean.MergedBeanDefinitionRegisteredEvent;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @author sibmaks
 * @since 0.0.12
 */
public final class JavaFlightRecorderMergedBeanDefinitionEventProducer implements MergedBeanDefinitionPostProcessor {
    private final ContextIdProvider contextIdProvider;
    private final ConfigurableListableBeanFactory beanFactory;

    public JavaFlightRecorderMergedBeanDefinitionEventProducer(
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

        var beanClass = BeanDefinitions.getBeanType(beanDefinition, beanType);
        var beanClassName = beanClass.getCanonicalName();
        var stereotype = BeanDefinitions.getStereotype(beanClass);
        var dependencies = BeanDefinitions.getDependencies(beanFactory, beanName, beanDefinition);
        var scope = BeanDefinitions.getScope(beanDefinition);

        var contextId = contextIdProvider.getContextId();
        MergedBeanDefinitionRegisteredEvent.builder()
                .contextId(contextId)
                .scope(scope)
                .beanClassName(beanClassName)
                .beanName(beanName)
                .primary(String.valueOf(beanDefinition.isPrimary()))
                .dependencies(DependencyConverter.convert(dependencies.toArray(String[]::new)))
                .stereotype(stereotype.name())
                .generated(false)
                .build()
                .commit();
    }

    private void produceGenerated(String beanName, Class<?> beanType) {
        var dependencies = Optional.of(beanFactory.getDependenciesForBean(beanName))
                .map(List::of)
                .map(HashSet::new)
                .orElseGet(HashSet::new);

        var stereotype = BeanDefinitions.getStereotype(beanType);
        var contextId = contextIdProvider.getContextId();
        var beanClassName = Optional.ofNullable(beanType)
                .map(Class::getCanonicalName)
                .orElse(null);

        MergedBeanDefinitionRegisteredEvent.builder()
                .contextId(contextId)
                .beanClassName(beanClassName)
                .beanName(beanName)
                .dependencies(DependencyConverter.convert(dependencies.toArray(String[]::new)))
                .generated(true)
                .stereotype(stereotype.name())
                .build()
                .commit();
    }
}
