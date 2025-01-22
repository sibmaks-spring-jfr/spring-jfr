package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.core.converter.DependencyConverter;
import io.github.sibmaks.spring.jfr.event.publish.bean.MergedBeanDefinitionRegisteredEvent;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

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

}
