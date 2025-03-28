package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.recording.bean.ResolveBeanDependencyEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author sibmaks
 * @since 0.0.18
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class JavaFlightRecorderResolveDependencyEventProducer implements BeanFactoryPostProcessor {
    private final ContextIdProvider contextIdProvider;

    public JavaFlightRecorderResolveDependencyEventProducer(ContextIdProvider contextIdProvider) {
        this.contextIdProvider = contextIdProvider;
    }

    @Pointcut(value = "execution(* org.springframework.beans.factory.config.AutowireCapableBeanFactory.resolveDependency(" +
            "org.springframework.beans.factory.config.DependencyDescriptor, String, ..)) && args(descriptor, beanName, ..)",
            argNames = "descriptor,beanName")
    public void resolveDependencyPointcut(DependencyDescriptor descriptor, String beanName) {
    }

    @Before(value = "resolveDependencyPointcut(descriptor, beanName)", argNames = "joinPoint,descriptor,beanName")
    public void logBeforeResolveDependency(JoinPoint joinPoint, DependencyDescriptor descriptor, String beanName) {
        if (descriptor == null || beanName == null) {
            return;
        }
        var dependencyName = descriptor.getDependencyName();
        if (dependencyName == null) {
            return;
        }
        var contextId = contextIdProvider.getContextId();
        ResolveBeanDependencyEvent.builder()
                .contextId(contextId)
                .dependentBeanName(beanName)
                .dependencyBeanName(dependencyName)
                .build()
                .commit();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
