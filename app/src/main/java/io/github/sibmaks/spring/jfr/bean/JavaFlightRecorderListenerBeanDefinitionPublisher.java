package io.github.sibmaks.spring.jfr.bean;

import io.github.sibmaks.spring.jfr.JavaFlightRecordingListener;
import io.github.sibmaks.spring.jfr.core.ContextIdProvider;
import io.github.sibmaks.spring.jfr.event.core.converter.DependencyConverter;
import io.github.sibmaks.spring.jfr.event.recording.bean.BeanDefinitionRegisteredEvent;
import jdk.jfr.Recording;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * @author sibmaks
 * @since 0.0.27
 */
@Slf4j
public class JavaFlightRecorderListenerBeanDefinitionPublisher implements JavaFlightRecordingListener {
    private final ApplicationContext applicationContext;
    private final ConfigurableListableBeanFactory beanFactory;
    private final String contextId;

    public JavaFlightRecorderListenerBeanDefinitionPublisher(
            ApplicationContext context,
            ConfigurableListableBeanFactory beanFactory,
            ContextIdProvider contextIdProvider
    ) {
        this.applicationContext = context;
        this.beanFactory = beanFactory;
        this.contextId = contextIdProvider.getContextId();
    }

    @Override
    public void onRunning(Recording recording) {
        try {
            var settings = recording.getSettings();
            var beansEnabled = settings.get("spring#bean.definitions.enabled");

            if (!"true".equals(beansEnabled)) {
                return;
            }
            var contextBeanDefinitionNames = applicationContext.getBeanDefinitionNames();
            var factoryBeanDefinitionNames = beanFactory.getBeanDefinitionNames();

            if (!Arrays.deepEquals(contextBeanDefinitionNames, factoryBeanDefinitionNames)) {
                log.warn("Bean names do not match");
            }

            var beansDependencies = getBeansDependencies(contextBeanDefinitionNames, factoryBeanDefinitionNames);

            for (var beanDependencies : beansDependencies.entrySet()) {
                var beanName = beanDependencies.getKey();
                var dependencies = beanDependencies.getValue();
                if (BeanFactoryUtils.isGeneratedBeanName(beanName)) {
                    produceGenerated(beanName, dependencies);
                } else {
                    produce(beanName, dependencies);
                }
            }
        } catch (Exception e) {
            log.error("Error encountered while enriching dependencies", e);
        }
    }

    private HashMap<String, Set<String>> getBeansDependencies(
            String[] contextBeanDefinitionNames,
            String[] factoryBeanDefinitionNames
    ) {
        var beansDependencies = new HashMap<String, Set<String>>();

        var toVisit = new LinkedList<String>();
        toVisit.addAll(List.of(contextBeanDefinitionNames));
        toVisit.addAll(List.of(factoryBeanDefinitionNames));

        var visited = new HashSet<String>();
        while (!toVisit.isEmpty()) {
            var name = toVisit.removeFirst();
            if (visited.contains(name)) {
                continue;
            }
            visited.add(name);

            var beanDependencies = beansDependencies.computeIfAbsent(name, k -> new HashSet<>());
            if (beanFactory.containsBeanDefinition(name)) {
                var beanDefinition = beanFactory.getBeanDefinition(name);
                var dependsOn = Optional.ofNullable(beanDefinition.getDependsOn()).orElse(new String[0]);
                beanDependencies.addAll(List.of(dependsOn));
                toVisit.addAll(List.of(dependsOn));
            }

            var dependencies = beanFactory.getDependenciesForBean(name);
            beanDependencies.addAll(List.of(dependencies));
            toVisit.addAll(List.of(dependencies));

            var dependents = beanFactory.getDependentBeans(name);
            toVisit.addAll(List.of(dependents));

            for (var dependent : dependents) {
                var otherBeanDependencies = beansDependencies.computeIfAbsent(dependent, k -> new HashSet<>());
                otherBeanDependencies.add(name);
            }
        }
        return beansDependencies;
    }

    private void produce(String beanName, Set<String> dependencies) {
        if (!beanFactory.containsBeanDefinition(beanName)) {
            produceGenerated(beanName, dependencies);
            return;
        }
        var beanType = beanFactory.getType(beanName);
        var beanClassName = Optional.ofNullable(beanType).map(Class::getCanonicalName).orElse(null);

        var beanDefinition = beanFactory.getMergedBeanDefinition(beanName);

        var stereotype = BeanDefinitions.getStereotype(beanType);
        var scope = BeanDefinitions.getScope(beanDefinition);

        BeanDefinitionRegisteredEvent.builder().contextId(contextId).scope(scope).actualBeanClassName(beanDefinition.getBeanClassName()).beanClassName(beanClassName).beanName(beanName).primary(String.valueOf(beanDefinition.isPrimary())).dependencies(DependencyConverter.convert(dependencies.toArray(String[]::new))).stereotype(stereotype.name()).generated(false).build().commit();
    }

    private void produceGenerated(String beanName, Set<String> dependencies) {
        var beanType = beanFactory.getType(beanName);
        var stereotype = BeanDefinitions.getStereotype(beanType);
        var beanClassName = Optional.ofNullable(beanType).map(Class::getCanonicalName).orElse(null);

        BeanDefinitionRegisteredEvent.builder().contextId(contextId).beanClassName(beanClassName).beanName(beanName).dependencies(DependencyConverter.convert(dependencies.toArray(String[]::new))).stereotype(stereotype.name()).generated(true).build().commit();
    }
}
