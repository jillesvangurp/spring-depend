# Spring-depend

Simple tool for analyzing spring dependencies. Such tools exist but they seemed to be hopelessly bundled with crappy IDE plugins. This is a standalone thing. All you need is a spring application context.

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
# License

[MIT](LICENSE)
