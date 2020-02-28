package com.jillesvangurp.springdepend.spring.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BeanName1 {
    @Autowired
    BeanName3 beanName3;

    @Autowired
    BeanName5 beanName5;
}
