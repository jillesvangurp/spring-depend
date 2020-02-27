package com.jillesvangurp.springdepend.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class Thingy2 {
    // just a mock class so we can pretend to create lots of beans
    @Autowired
    Thingy3 thingy3;

    @Autowired
    @Qualifier("bean5")
    Thingy thingy;
}
