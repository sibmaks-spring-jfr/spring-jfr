package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.core.converter.DependencyConverter;
import io.github.sibmaks.spring.jfr.event.recording.bean.BeanDefinitionRegisteredEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @author sibmaks
 * @since 0.0.2
 */
public final class JavaFlightRecorderBeanDefinitionEventProducer implements BeanDefinitionRegistryPostProcessor {
    private final ContextIdProvider contextIdProvider;

    public JavaFlightRecorderBeanDefinitionEventProducer(
            ContextIdProvider contextIdProvider
    ) {
        this.contextIdProvider = contextIdProvider;
    }

    private void produce(
            ConfigurableListableBeanFactory beanFactory,
            String beanName,
            Class<?> beanType
    ) {
        if (!beanFactory.containsBeanDefinition(beanName)) {
            produceGenerated(beanFactory, beanName, beanType);
            return;
        }

        var beanDefinition = beanFactory.getMergedBeanDefinition(beanName);

        var stereotype = BeanDefinitions.getStereotype(beanType);
        var dependencies = BeanDefinitions.getDependencies(beanFactory, beanName, beanDefinition);
        var scope = BeanDefinitions.getScope(beanDefinition);

        var contextId = contextIdProvider.getContextId();
        BeanDefinitionRegisteredEvent.builder()
                .contextId(contextId)
                .scope(scope)
                .actualBeanClassName(beanDefinition.getBeanClassName())
                .beanClassName(beanType.getCanonicalName())
                .beanName(beanName)
                .primary(String.valueOf(beanDefinition.isPrimary()))
                .dependencies(DependencyConverter.convert(dependencies.toArray(String[]::new)))
                .stereotype(stereotype.name())
                .generated(false)
                .build()
                .commit();
    }

    private void produceGenerated(
            ConfigurableListableBeanFactory beanFactory,
            String beanName,
            Class<?> beanType
    ) {
        var dependencies = Optional.of(beanFactory.getDependenciesForBean(beanName))
                .map(List::of)
                .map(HashSet::new)
                .orElseGet(HashSet::new);

        var stereotype = BeanDefinitions.getStereotype(beanType);
        var contextId = contextIdProvider.getContextId();
        var beanClassName = Optional.ofNullable(beanType)
                .map(Class::getCanonicalName)
                .orElse(null);

        BeanDefinitionRegisteredEvent.builder()
                .contextId(contextId)
                .beanClassName(beanClassName)
                .beanName(beanName)
                .dependencies(DependencyConverter.convert(dependencies.toArray(String[]::new)))
                .stereotype(stereotype.name())
                .generated(true)
                .build()
                .commit();
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // not required for this implementation
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        var beanDefinitionNames = beanFactory.getBeanDefinitionNames();

        for (var beanName : beanDefinitionNames) {
            var beanType = beanFactory.getType(beanName);
            if (BeanFactoryUtils.isGeneratedBeanName(beanName)) {
                produceGenerated(beanFactory, beanName, beanType);
            } else {
                produce(beanFactory, beanName, beanType);
            }
        }
    }
}
