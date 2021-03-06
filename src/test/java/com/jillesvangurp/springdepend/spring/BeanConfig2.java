package com.jillesvangurp.springdepend.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({BeanConfig1.class})
public class BeanConfig2 {

    @Bean
    public Thingy bean3() {
        return new Thingy();
    }

    @Bean
    public Thingy bean4(Thingy bean1,Thingy bean3) {
        return new Thingy();
    }

    @Bean
    public Thingy bean5(Thingy bean4) {
        return new Thingy();
    }

    @Bean
    public Thingy2 bean6() {
        return new Thingy2();
    }

    @Bean
    public Thingy3 bean7() {
        return new Thingy3();
    }
}
