package com.jillesvangurp.springdepend.json;

import java.util.List;

public class BeanDependency {
    private Integer injectedBeanCount;
    private List<String> injectedBeanNames;
    private Integer circularDependencyCount;
    private List<String> circularDependencyDescriptions;

    public BeanDependency() {
    }

    public BeanDependency(Integer injectedBeanCount, List<String> injectedBeanNames, Integer circularDependencyCount, List<String> circularDependencyDescriptions) {
        this.injectedBeanCount = injectedBeanCount;
        this.injectedBeanNames = injectedBeanNames;
        this.circularDependencyCount = circularDependencyCount;
        this.circularDependencyDescriptions = circularDependencyDescriptions;
    }

    public Integer getInjectedBeanCount() {
        return injectedBeanCount;
    }

    public void setInjectedBeanCount(Integer injectedBeanCount) {
        this.injectedBeanCount = injectedBeanCount;
    }

    public List<String> getInjectedBeanNames() {
        return injectedBeanNames;
    }

    public void setInjectedBeanNames(List<String> injectedBeanNames) {
        this.injectedBeanNames = injectedBeanNames;
    }

    public Integer getCircularDependencyCount() {
        return circularDependencyCount;
    }

    public void setCircularDependencyCount(Integer circularDependencyCount) {
        this.circularDependencyCount = circularDependencyCount;
    }

    public List<String> getCircularDependencyDescriptions() {
        return circularDependencyDescriptions;
    }

    public void setCircularDependencyDescriptions(List<String> circularDependencyDescriptions) {
        this.circularDependencyDescriptions = circularDependencyDescriptions;
    }
}
