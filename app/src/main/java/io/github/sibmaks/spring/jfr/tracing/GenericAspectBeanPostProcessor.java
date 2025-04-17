package io.github.sibmaks.spring.jfr.tracing;

import lombok.Setter;
import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.27
 */
@Aspect
public abstract class GenericAspectBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {
    protected final List<String> filters;
    @Setter
    protected BeanFactory beanFactory;

    public GenericAspectBeanPostProcessor(
            List<String> filters
    ) {
        this.filters = filters;
    }

    /**
     * Check is bean type match passed filter
     *
     * @param beanClass bean type
     * @param filter    filter
     * @return true - bean match, false - otherwise
     */
    public static boolean matchesBean(Class<?> beanClass, String filter) {
        var className = beanClass.getName();

        if (!filter.endsWith(".*")) {
            return className.equals(filter);
        }
        var packagePath = filter.substring(0, filter.length() - 2);
        return className.startsWith(packagePath + ".");
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        var type = getActualBeanType(bean);

        if (!isAspectBean(bean, type, beanName)) {
            return bean;
        }

        for (var entry : filters) {
            if (!matchesBean(type, entry)) {
                continue;
            }
            return buildProxy(bean, type);
        }

        return bean;
    }

    protected Class<?> getActualBeanType(Object bean) {
        return AopProxyUtils.ultimateTargetClass(bean);
    }

    private Object buildProxy(Object bean, Class<?> type) {
        var advice = buildAdvice(bean, type);
        if (bean instanceof Advised) {
            var advised = (Advised) bean;
            advised.addAdvice(advice);
            return bean;
        }

        var proxyFactory = new AspectJProxyFactory(bean);
        proxyFactory.setProxyTargetClass(!type.isInterface());
        proxyFactory.addAdvice(advice);
        return proxyFactory.getProxy();
    }

    protected abstract Advice buildAdvice(Object bean, Class<?> type);

    protected abstract boolean isAspectBean(Object bean, Class<?> type, String beanName);
}
