package com.jillesvangurp.springdepend.spring.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BeanName11 {
    @Autowired
    BeanName12 beanName12;
}
