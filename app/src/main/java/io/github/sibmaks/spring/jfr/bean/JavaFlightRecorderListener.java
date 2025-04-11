package io.github.sibmaks.spring.jfr.bean;

import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * @author sibmaks
 * @since 0.0
 */
@Slf4j
@AllArgsConstructor
public class JavaFlightRecorderListener implements FlightRecorderListener {
    private final ApplicationContext applicationContext;
    private final ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Override
    public void recordingStateChanged(Recording recording) {
        try {
            log.debug("Recorder stateChanged: {} {}", recording.getName(), recording.getState());
            if (recording.getState() != RecordingState.RUNNING) {
                return;
            }
            var settings = recording.getSettings();
            var beansEnabled = settings.get("spring#bean.definitions.enabled");

            if (!"true".equals(beansEnabled)) {
                return;
            }
            var contextBeanDefinitionNames = applicationContext.getBeanDefinitionNames();
            var factoryBeanDefinitionNames = configurableListableBeanFactory.getBeanDefinitionNames();

            if (!Arrays.deepEquals(contextBeanDefinitionNames, factoryBeanDefinitionNames)) {
                log.warn("Bean names do not match");
            }

            var visited = new HashSet<String>();
            var toVisit = new LinkedList<String>();
            toVisit.addAll(List.of(contextBeanDefinitionNames));
            toVisit.addAll(List.of(factoryBeanDefinitionNames));

            var beansDependencies = new HashMap<String, Set<String>>();

            while (!toVisit.isEmpty()) {
                var name = toVisit.removeFirst();
                if (visited.contains(name)) {
                    continue;
                }
                visited.add(name);

                var beanDependencies = beansDependencies.computeIfAbsent(name, k -> new HashSet<>());
                if (configurableListableBeanFactory.containsBeanDefinition(name)) {
                    var beanDefinition = configurableListableBeanFactory.getBeanDefinition(name);
                    var dependsOn = Optional.ofNullable(beanDefinition.getDependsOn()).orElse(new String[0]);
                    beanDependencies.addAll(List.of(dependsOn));
                    toVisit.addAll(List.of(dependsOn));
                }

                var dependencies = configurableListableBeanFactory.getDependenciesForBean(name);
                beanDependencies.addAll(List.of(dependencies));
                toVisit.addAll(List.of(dependencies));

                var dependents = configurableListableBeanFactory.getDependentBeans(name);
                toVisit.addAll(List.of(dependents));

                for (var dependent : dependents) {
                    var otherBeanDependencies = beansDependencies.computeIfAbsent(dependent, k -> new HashSet<>());
                    otherBeanDependencies.add(name);
                }
            }
        } catch (Exception e) {
            log.error("Error encountered while enriching dependencies", e);
        }
    }
}
