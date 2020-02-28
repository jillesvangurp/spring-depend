package com.jillesvangurp.springdepend.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({BeanConfig2.class})
@ComponentScan({"com.jillesvangurp.springdepend.spring.scan"})
public class RootConfig {
}
