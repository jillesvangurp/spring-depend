package com.jillesvangurp.springdepend.spring.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BeanName {

    @Autowired
    BeanName1 beanName1;

    @Autowired
    BeanName2 beanName2;
}
