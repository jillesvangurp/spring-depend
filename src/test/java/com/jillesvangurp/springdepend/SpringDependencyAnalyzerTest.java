package com.jillesvangurp.springdepend;


import static org.assertj.core.api.Assertions.assertThat;

import com.jillesvangurp.springdepend.spring.RootConfig;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
@ContextConfiguration(classes = RootConfig.class)
public class SpringDependencyAnalyzerTest extends AbstractTestNGSpringContextTests {

    private SpringDependencyAnalyzer analyzer;

    @BeforeMethod
    public void before() {
        GenericApplicationContext context =  (GenericApplicationContext) super.applicationContext;
        analyzer = new SpringDependencyAnalyzer(context);
    }

    public void shouldDumpReportWithoutExceptions() {
        analyzer.printReport(RootConfig.class);
    }

    public void shouldBuildDepGraph() {
        SimpleGraph<String> beanGraph = analyzer.getBeanGraph();
        // FIXME more elaborate asserts once I get this right
        assertThat(beanGraph.toString()).contains("bean1","bean5");
    }

}
