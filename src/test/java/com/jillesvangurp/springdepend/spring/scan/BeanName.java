package com.jillesvangurp.springdepend.spring.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BeanName {

    @Autowired
    BeanName11 beanName11;

    @Autowired
    BeanName21 beanName21;

    @Autowired
    BeanName31 beanName31;
}
