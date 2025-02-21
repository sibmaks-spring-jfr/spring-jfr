package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.core.converter.DependencyConverter;
import io.github.sibmaks.spring.jfr.event.recording.bean.MergedBeanDefinitionRegisteredEvent;
import org.springframework.beans.BeansException;
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
    public void postProcessMergedBeanDefinition(
            RootBeanDefinition beanDefinition,
            Class<?> beanType,
            String beanName
    ) {
        produce(beanDefinition, beanName, beanType);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        var beanType = bean.getClass();
        if (BeanFactoryUtils.isGeneratedBeanName(beanName)) {
            produceGenerated(beanName, beanType);
            return bean;
        }
        if (!beanFactory.containsBeanDefinition(beanName)) {
            produceGenerated(beanName, beanType);
            return bean;
        }
        var beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
        produce(beanDefinition, beanName, beanType);
        return bean;
    }

    private void produce(BeanDefinition beanDefinition, String beanName, Class<?> beanType) {
        var stereotype = BeanDefinitions.getStereotype(beanType);
        var dependencies = BeanDefinitions.getDependencies(beanFactory, beanName, beanDefinition);
        var scope = BeanDefinitions.getScope(beanDefinition);

        var contextId = contextIdProvider.getContextId();
        MergedBeanDefinitionRegisteredEvent.builder()
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

    private void produceGenerated(String beanName, Class<?> beanType) {
        var dependencies = Optional.of(beanFactory.getDependenciesForBean(beanName))
                .map(List::of)
                .map(HashSet::new)
                .orElseGet(HashSet::new);

        var contextId = contextIdProvider.getContextId();
        var beanClassName = beanType.getCanonicalName();

        MergedBeanDefinitionRegisteredEvent.builder()
                .contextId(contextId)
                .beanClassName(beanClassName)
                .beanName(beanName)
                .dependencies(DependencyConverter.convert(dependencies.toArray(String[]::new)))
                .generated(true)
                .build()
                .commit();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
