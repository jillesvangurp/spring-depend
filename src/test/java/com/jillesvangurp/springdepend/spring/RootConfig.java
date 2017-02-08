package com.jillesvangurp.springdepend.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({BeanConfig2.class})
public class RootConfig {
}
