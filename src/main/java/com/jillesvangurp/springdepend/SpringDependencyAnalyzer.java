package com.jillesvangurp.springdepend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jillesvangurp.springdepend.json.BeanDependency;
import com.jillesvangurp.springdepend.json.BeanDependencyStatistic;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Spring dependency analyzer that works with any GenericApplicationContext.
 */
public class SpringDependencyAnalyzer {
    private final GenericApplicationContext context;

    /**
     * @param context create your spring context the usual way and inject it here.
     */
    public SpringDependencyAnalyzer(GenericApplicationContext context) {
        this.context = context;
    }

    /**
     * Long lists of dependencies indicate low cohesiveness and high coupling. This helps you identify the problematic beans.
     *
     * @return map of dependencies for all beans in the context
     */
    public Map<String, Set<String>> getBeanDependencies() {
        Map<String, Set<String>> beanDeps = new TreeMap<>();
        ConfigurableListableBeanFactory factory = context.getBeanFactory();
        for (String beanName : factory.getBeanDefinitionNames()) {
            if (factory.getBeanDefinition(beanName).isAbstract()) {
                continue;
            }
            String[] dependenciesForBean = factory.getDependenciesForBean(beanName);
            Set<String> set = beanDeps.get(beanName);
            if (set == null) {
                set = new TreeSet<>();
                beanDeps.put(beanName, set);
            }
            for (String dependency : dependenciesForBean) {
                set.add(dependency);
            }
        }
        return beanDeps;
    }

    /**
     * If you have a lot of beans that are not depended on or only once, maybe they shouldn't be a bean at all.
     *
     * @return map of reverse dependencies for all beans in the context
     */
    public Map<String, Set<String>> getReverseBeanDependencies() {
        Map<String, Set<String>> reverseBeanDeps = new TreeMap<>();
        Map<String, Set<String>> beanDeps = getBeanDependencies();

        beanDeps.forEach((beanName, deps) -> {
            for (String dep : deps) {
                Set<String> set = reverseBeanDeps.get(dep);
                if (set == null) {
                    set = new TreeSet<>();
                    reverseBeanDeps.put(dep, set);
                }
                set.add(beanName);
            }
        });

        return reverseBeanDeps;
    }

    /**
     * Organizes the graph of configuration classes in layers that depend on each other.
     * Classes in the same layer can only import classes in lower layers. Spring does not allow import cycles.
     * A good pattern is to have a RootConfig for your application that simply imports everything else you need.
     * The more layers you have the more complex your dependencies.
     *
     * @param configurationClass the root configuration class that you want to analyze
     * @return treemap with layers of configuratino
     */
    public Map<Integer, Set<Class<?>>> getConfigurationLayers(Class<?> configurationClass) {

        SimpleGraph<Class<?>> rootGraph = getConfigurationGraph(configurationClass);
        return rootGraph.getLayers();
    }

    private void validateIsConfigurationClass(Class<?> configurationClass) {
        boolean isConfigClass = false;
        for (Annotation annotation : configurationClass.getAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (Configuration.class.equals(type)) {
                isConfigClass = true;
            }
        }
        if (!isConfigClass) {
            throw new IllegalArgumentException("not a spring configuration class");
        }
    }

    /**
     * @param configurationClass spring configuration root class from which to calculate the configuration hierarchy
     * @return a graph of the configuration classes
     */
    public SimpleGraph<Class<?>> getConfigurationGraph(Class<?> configurationClass) {
        validateIsConfigurationClass(configurationClass);
        return SimpleGraph.treeBuilder(configurationClass, SpringDependencyAnalyzer::getConfigurationImportsFor);
    }

    public SimpleGraph<String> getBeanGraph() {
        Map<String, Set<String>> beanDeps = getBeanDependencies();
        Map<String, Set<String>> reverseBeanDeps = getReverseBeanDependencies();

        SimpleGraph<String> graph = new SimpleGraph<>();

        beanDeps.forEach((bean, deps) -> {
            if (deps.isEmpty()) {
                // bean has no deps, so we can figure out everything that depends on this bean here
                SimpleGraph<String> depGraph = new SimpleGraph<>();
                Set<String> simpleGraphs = new HashSet<>();
                SimpleGraph.buildGraph(depGraph, bean, b -> reverseBeanDeps.get(b), simpleGraphs);
                graph.put(bean, depGraph);
            }
        });
        // FIXME technically this is a reverse dependency graph, We need to revert it.
        return graph;
    }

    private static List<Class<?>> getConfigurationImportsFor(Class<?> clazz) {
        List<Class<?>> list = new ArrayList<>();
        for (Annotation annotation : clazz.getAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (Import.class.equals(type)) {
                try {
                    Method method = type.getMethod("value");
                    Class<?>[] imports = (Class<?>[]) method.invoke(annotation, (Object[]) null);
                    if (imports != null && imports.length > 0) {
                        for (Class<?> c : imports) {
                            list.add(c);
                        }
                    }
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return list;
    }

    public String configurationGraphCypher(Class<?> rootClass) {
        return getConfigurationGraph(rootClass).toCypher("ConfigClass", "Imports", c -> c.getSimpleName());
    }

    public String beanGraphCypher() {
        return getBeanGraph().toCypher("Bean", "DEPENDSON", s -> s.replace(".", "_").replace("-", "__"));
    }

    public void printReport(Class<?> springConfigurationClass) {
        System.err.println("Configuration layers:\n");
        getConfigurationLayers(springConfigurationClass).forEach((layer, classes) -> {
            System.err.println("" + layer + "\t" + StringUtils.join(classes, ','));
        });

        System.err.println("\n\nDependencies:\n");
        Map<String, Set<String>> beanDependencies = getBeanDependencies();
        beanDependencies.forEach((name, dependencies) -> {
            System.err.println(name + ": " + StringUtils.join(dependencies, ','));
        });
        System.err.println("\n\nReverse dependencies:\n");
        Map<String, Set<String>> reverseBeanDependencies = getReverseBeanDependencies();
        reverseBeanDependencies.forEach((name, dependencies) -> {
            System.err.println(name + ": " + StringUtils.join(dependencies, ','));
        });

        System.err.println("\n\nBean dependency graph:\n");
        System.err.println(getBeanGraph());
        System.err.println("Bean layers:\n");

        getBeanGraph().getLayers().forEach((layer, classes) -> {
            System.err.println("" + layer + "\t" + StringUtils.join(classes, ','));
        });
    }

    public String getCircularDependencyStatisticJson() {

        Map<String, Set<String>> beanDependencies = getBeanDependencies();
        LinkedHashMap<String, BeanDependency> map = new LinkedHashMap<>();
        beanDependencies.forEach((name, dependencies) -> {
            Set<String> circularDependencyDescriptions = new HashSet<>();
            findCycleDependencies(circularDependencyDescriptions, beanDependencies, dependencies, new LinkedHashSet<>(), name, 0, 4);
            map.put(name, new BeanDependency(dependencies.size(),
                    new ArrayList<>(dependencies),
                    circularDependencyDescriptions.size(),
                    new ArrayList<>(circularDependencyDescriptions)));
        });
        int count = 0;
        for (Map.Entry<String, BeanDependency> stringBeanDependencyEntry : map.entrySet()) {
            count = count + stringBeanDependencyEntry.getValue().getCircularDependencyCount();
        }
        LinkedHashMap<String, BeanDependency> collect = map.entrySet().stream().sorted((o1, o2) -> {
            BeanDependency value1 = o1.getValue();
            BeanDependency value2 = o2.getValue();
            int i = value2.getCircularDependencyCount().compareTo(value1.getCircularDependencyCount());
            return i == 0 ? o1.getKey().compareTo(o2.getKey()) : i;
        }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        BeanDependencyStatistic beanDependencyStatistic = new BeanDependencyStatistic();
        beanDependencyStatistic.setCreateDate(new Date());
        beanDependencyStatistic.setDependencyMap(collect);
        beanDependencyStatistic.setAllBeanCircularDependencyCount(count);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String result = gson.toJson(beanDependencyStatistic);
        return result;
    }


    private void findCycleDependencies(Set<String> circularDependencyDescriptions, Map<String, Set<String>> allBeansDependenciesMap, Set<String> dependecies, Set<String> dependencyNameChain, String targetName, int currentDepth, int maxDepth) {
        currentDepth++;
        int stepDepthValue = currentDepth;
        for (String dep : dependecies) {
            Set<String> currentStepNameChain = new LinkedHashSet<>(dependencyNameChain);
            Set<String> dependeciesOfBeanDep = allBeansDependenciesMap.get(dep);
            if (dependeciesOfBeanDep == null || dependeciesOfBeanDep.isEmpty()) {
                currentStepNameChain.remove(dep);
                continue;
            }
            if (dependeciesOfBeanDep.contains(targetName) && !currentStepNameChain.contains(targetName)) {
                StringBuilder sb = new StringBuilder(targetName);
                if (!currentStepNameChain.isEmpty()) sb.append('-').append(StringUtils.join(currentStepNameChain, '-'));
                sb.append('-').append(dep).append('-').append(targetName);
                circularDependencyDescriptions.add(sb.toString());
            }
            if (stepDepthValue + 1 <= maxDepth) {
                currentStepNameChain.add(dep);
                findCycleDependencies(circularDependencyDescriptions, allBeansDependenciesMap, dependeciesOfBeanDep, currentStepNameChain, targetName, stepDepthValue, maxDepth);
            }

        }
    }
}
