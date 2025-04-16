package io.github.sibmaks.spring.jfr.tracing;

import org.aspectj.lang.annotation.Aspect;
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
            if (!AspectUtils.matchesBean(type, entry)) {
                continue;
            }
            return buildProxy(bean, type);
        }

        return bean;
    }

    private Object buildProxy(Object bean, Class<?> type) {
        var aspect = buildAspect(bean, type);
        return AspectUtils.buildProxy(bean, aspect);
    }

    protected abstract Object buildAspect(Object bean, Class<?> type);
}
