package io.github.sibmaks.spring.jfr.tracing;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.27
 */
@Aspect
public abstract class GenericAspectBeanPostProcessor implements BeanPostProcessor {
    protected final List<String> filters;

    public GenericAspectBeanPostProcessor(List<String> filters) {
        this.filters = filters;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        var type = AopUtils.getTargetClass(bean);

        for (var entry : filters) {
            if (!matchesBean(type, entry)) {
                continue;
            }
            return buildProxy(bean, type);
        }

        return bean;
    }

    private Object buildProxy(Object bean, Class<?> type) {
        var advice = buildAdvice(bean, type);
        if(bean instanceof Advised) {
            var advised = (Advised) bean;
            advised.addAdvice(advice);
            return bean;
        }

        var proxyFactory = new ProxyFactory(bean);
        proxyFactory.setProxyTargetClass(!type.isInterface());
        proxyFactory.addAdvice(advice);
        return proxyFactory.getProxy();
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

    protected abstract Advice buildAdvice(Object bean, Class<?> type);
}
