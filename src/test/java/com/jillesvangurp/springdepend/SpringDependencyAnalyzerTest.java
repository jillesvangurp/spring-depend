package com.jillesvangurp.springdepend;


import static org.assertj.core.api.Assertions.assertThat;

import com.jillesvangurp.springdepend.spring.RootConfig;
import java.util.Locale;
import org.springframework.context.support.AbstractApplicationContext;
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
        AbstractApplicationContext context =  (AbstractApplicationContext) super.applicationContext;
        analyzer = new SpringDependencyAnalyzer(context);
    }

    public void shouldDumpReportWithoutExceptions() {
        analyzer.printReport(RootConfig.class);
    }

    public void shouldBuildDepGraph() {
        SimpleGraph<String> beanGraph = analyzer.getBeanGraph();
        // FIXME more elaborate asserts once I get this right

        assertThat(beanGraph.toString()).contains("beanName32","beanName31");

        System.err.println(beanGraph.toCypher("bean", "dependsOn", s -> s.replace(".", "_").toLowerCase(Locale.ENGLISH)));
    }

    public void shouldPrintCircularDependencyStatistic() {
        String circularDependencyStatisticJson = analyzer.getCircularDependencyStatisticJson();
        System.err.println(circularDependencyStatisticJson);
        assertThat(circularDependencyStatisticJson).contains("beanName-beanName31-beanName32-beanName");
    }

}
