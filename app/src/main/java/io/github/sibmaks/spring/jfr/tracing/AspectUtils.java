package io.github.sibmaks.spring.jfr.tracing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

/**
 * @author sibmaks
 * @since 0.0.27
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AspectUtils {
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

    /**
     * Build proxy for specific bean using passed aspect
     *
     * @param bean   bean instance
     * @param aspect aspecting
     * @return proxied bean
     */
    public static Object buildProxy(Object bean, Object aspect) {
        var proxyFactory = new AspectJProxyFactory(bean);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAspect(aspect);
        return proxyFactory.getProxy();
    }
}
