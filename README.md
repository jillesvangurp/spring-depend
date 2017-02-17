# Spring-depend

Tool for analyzing spring dependencies exporting your dependencies to neo4j. Similar tools exist but they seem to be bundled with complicated IDE plugins or other stuff I don't really need/use. This makes standalone usage a bit hard. This is a standalone thing. All you need to use this is a spring application context. 

To use: 
 - Add the dependency to spring-depend to your existing spring project
 - Get a reference to your application context in one of the several ways that spring allows you to do this. Any instance of spring's `GenericApplicationContext` or one of the sub classes should work
 - Do something like this:
```
SpringDependencyAnalyzer analyzer = new SpringDependencyAnalyzer(context);
analyzer.printReport();
System.out.println(analyzer.beanGraphCypher()) // copy paste in neo4j console & enjoy!
```

Features:
  - `Map<String, Set<String>> getBeanDependencies()` returns a map of all the beans in your context and their dependencies. Needing lots of things is a sign of low coherence and high coupling.
  - `Map<String, Set<String>> getReverseBeanDependencies()` returns reverse dependencies. Being used a lot is a good thing; it indicates usefulness. Things that are rarely used might not need to be beans on the other hand.
  - `SimpleGraph<String> getBeanGraph()` a graph of the dependencies. (TODO: technically this is a reverse dependency graph that is generated from both the dependency and reverse dependency map. I need to add the ability to invert it.)
  - `SimpleGraph<Class<?>> getConfigurationGraph(Class<?> configurationClass)` return a graph of your `@Configuration` classes by following the imports from the specified root class.
  - `Map<Integer, Set<Class<?>>> getConfigurationLayers(Class<?> configurationClass)` returns a tree map with the configuration classes ordered in layers by their dependencies on each other. The more layers you need, the more complex your spring dependencies are. Consider refactoring them to have less interdependencies. Untangling the the most coupled beans will likely clear this up.  
  - `String configurationGraphCypher(Class<?>)` returns neo4j cypher for your Spring configuration import dependencies in neo4j
  - `String beanGraphCypher()` returns neo4j cypher for creating your spring bean dependency graph in neo4j


# Future work
When time allows, I might work on these topics a bit. Pull requests are welcome of course.

  - Better graph implementation than the rather limited `SimpleGraph` currently included. This was a quick and dirty job but it seems good enough. 
  - Fix the bean graph to not be a reverse dependency graph.
  - Simple metrics for coherence and coupling.
  - Test framework support so you can assert constraints on your dependencies and related metrics from a simple unit test.

# Get it from Maven Central

```
<dependency>
    <groupId>com.jillesvangurp</groupId>
    <artifactId>spring-depend</artifactId>
    <!-- check maven central for latest, Readme's get out of date so easily ... -->
    <version>0.2</version>
</dependency>
```

Note. the spring dependency in the pom is optional. This is so you can specify which spring version you use and avoid nasty conflicts. Anything recent (4.x and up) should pretty much work with this and perhaps older versions too. 

# License

[MIT](LICENSE)
