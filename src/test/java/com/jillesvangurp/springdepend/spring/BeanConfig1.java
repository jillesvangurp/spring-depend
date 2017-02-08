package com.jillesvangurp.springdepend.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig1 {

    @Bean
    public Thingy bean1() {
        return new Thingy();
    }

    @Bean
    public Thingy bean2(Thingy bean1) {
        return new Thingy();
    }
}
