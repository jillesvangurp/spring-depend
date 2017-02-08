# Spring-depend

Simple tool for analyzing spring dependencies. Such tools exist but they seemed to be bundled with complicated IDE plugins, which makes standalone usage a bit hard. This is a standalone thing. All you need is a spring application context.

  - `Map<String, Set<String>> getBeanDependencies()` returns a list of all the beans in your context and their dependencies
  - `Map<String, Set<String>> getReverseBeanDependencies()` returns reverse dependencies
  - `Map<Integer, Set<Class<?>>> getConfigurationLayers(Class<?> configurationClass)` returns a tree map with the configuration classes ordered in layers by their dependencies on each other
  - dump a report of bean dependencies and reverse dependencies
  - dump a report of how  `@Configuration` classes are layered

# Get it from Maven Central

```
<dependency>
    <groupId>com.jillesvangurp</groupId>
    <artifactId>spring-depend</artifactId>
    <version>0.1</version>
</dependency>
```

Note. the spring dependency in the pom is optional. This allows you to specify which spring version you use. Anything recent (4.x and up) should pretty much work. Beyond that you might have to fix a thing or two to get stuff working.

# License

[MIT](LICENSE)
