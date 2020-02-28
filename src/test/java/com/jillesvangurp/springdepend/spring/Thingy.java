package com.jillesvangurp.springdepend.spring;

import org.springframework.beans.factory.annotation.Autowired;

public class Thingy {
    // just a mock class so we can pretend to create lots of beans
    @Autowired
    Thingy3 thingy;
}
