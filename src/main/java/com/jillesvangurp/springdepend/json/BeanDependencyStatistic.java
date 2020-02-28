package com.jillesvangurp.springdepend.json;

import java.util.Date;
import java.util.Map;

public class BeanDependencyStatistic {

    private Date createDate;
    private Integer allBeanCircularDependencyCount;
    private Map<String, BeanDependency> dependencyMap;

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getAllBeanCircularDependencyCount() {
        return allBeanCircularDependencyCount;
    }

    public void setAllBeanCircularDependencyCount(Integer allBeanCircularDependencyCount) {
        this.allBeanCircularDependencyCount = allBeanCircularDependencyCount;
    }

    public Map<String, BeanDependency> getDependencyMap() {
        return dependencyMap;
    }

    public void setDependencyMap(Map<String, BeanDependency> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }
}
