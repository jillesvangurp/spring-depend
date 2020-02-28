package com.jillesvangurp.springdepend.spring.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BeanName12 {

    @Autowired
    BeanName13 beanName13;
}
