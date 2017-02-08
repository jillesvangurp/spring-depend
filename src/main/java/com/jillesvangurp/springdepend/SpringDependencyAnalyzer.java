package com.jillesvangurp.springdepend;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

public class SpringDependencyAnalyzer {
    private final GenericApplicationContext context;

    public static class ClassGraph extends LinkedHashMap<Class<?>, ClassGraph> {
        private static final long serialVersionUID = 2744375702301542235L;

        public ClassGraph() {
            super();
        }
    }

    public SpringDependencyAnalyzer(GenericApplicationContext context) {
        this.context = context;
    }

    public Map<String, Set<String>> getBeanDependencies() {
        Map<String, Set<String>> beanDeps = new TreeMap<>();
        ConfigurableListableBeanFactory factory = context.getBeanFactory();
        for(String beanName : factory.getBeanDefinitionNames()) {
            if(factory.getBeanDefinition(beanName).isAbstract()) {
                continue;
            }
            String[] dependenciesForBean = factory.getDependenciesForBean(beanName);
            Set<String> set = beanDeps.get(beanName);
            if(set == null) {
                set = new TreeSet<>();
                beanDeps.put(beanName, set);
            }
            for(String dependency : dependenciesForBean) {
                set.add(dependency);
            }
        }
        return beanDeps;
    }

    public Map<String, Set<String>> getReverseBeanDependencies() {
        Map<String, Set<String>> reverseBeanDeps = new TreeMap<>();
        Map<String, Set<String>> beanDeps = getBeanDependencies();

        beanDeps.forEach((beanName,deps) -> {
            for(String dep: deps) {
                Set<String> set = reverseBeanDeps.get(dep);
                if(set == null) {
                    set = new TreeSet<>();
                    reverseBeanDeps.put(dep, set);
                }
                set.add(beanName);
            }
        });

        return reverseBeanDeps;
    }

    public Map<Integer, Set<Class<?>>> getConfigurationLayers(Class<?> configurationClass) {
        boolean isConfigClass = false;
        for(Annotation annotation : configurationClass.getAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if(Configuration.class.equals(type)) {
                isConfigClass = true;
            }
        }
        if(!isConfigClass) {
            throw new IllegalArgumentException("not a spring configuration class");
        }

        ClassGraph subDependencies = buildConfigurationImportGraph(new ClassGraph(), configurationClass);
        ClassGraph rootGraph = new ClassGraph();
        rootGraph.put(configurationClass, subDependencies);


        Map<Class<?>, Integer> layerMap = new HashMap<>();
        buildConfigurationLayers(layerMap, rootGraph, 0);

        Map<Integer,Set<Class<?>>> layers = new TreeMap<>();
        layerMap.forEach((k, v) -> {
            Set<Class<?>> set = layers.get(v);
            if(set==null){
                set=new LinkedHashSet<>();
                layers.put(v, set);
            }
            set.add(k);
        });
        return layers;
    }

    private void buildConfigurationLayers(Map<Class<?>, Integer> layerMap, ClassGraph current, int depth) {
        current.forEach((k, v) -> {
            Integer kCount = layerMap.get(k);
            if(kCount == null || kCount < depth) {
                layerMap.put(k, depth);
            }
            buildConfigurationLayers(layerMap, v, depth + 1);
        });
    }

    private static ClassGraph buildConfigurationImportGraph(ClassGraph parent, Class<?> clazz) {
        List<Class<?>> imports = getConfigurationImports(clazz);

        for(Class<?> c : imports) {
            parent.put(c, buildConfigurationImportGraph(new ClassGraph(), c));
        }
        return parent;
    }

    private static List<Class<?>> getConfigurationImports(Class<?> clazz) {
        List<Class<?>> list= new ArrayList<>();
        for(Annotation annotation : clazz.getAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if(Import.class.equals(type)) {
                try {
                    Method method = type.getMethod("value");
                    Class<?>[] imports = (Class<?>[]) method.invoke(annotation, (Object[]) null);
                    if(imports != null && imports.length > 0) {
                        for(Class<?> c: imports) {
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

    public void printReport(Class<?> springConfigurationClass) {
        Map<Integer, Set<Class<?>>> layers = getConfigurationLayers(springConfigurationClass);

        System.err.println("Configuration layers:\n");
        layers.forEach((layer,classes) -> {
            System.err.println("" + layer + "\t" + StringUtils.join(classes,','));
        });

        System.err.println("\n\nDependencies:\n");
        Map<String, Set<String>> beanDependencies = getBeanDependencies();
        beanDependencies.forEach((name,dependencies) -> {
            System.err.println(name + ": " + StringUtils.join(dependencies,','));
        });
        System.err.println("\n\nReverse dependencies:\n");
        Map<String, Set<String>> reverseBeanDependencies = getReverseBeanDependencies();
        reverseBeanDependencies.forEach((name,dependencies) -> {
            System.err.println(name + ": " + StringUtils.join(dependencies,','));
        });
    }
}
