# chained
Java annotation processor to generate chained immutable builders including map and list builders.
* chained builders provide compile-time *type-safety* to ensure mandatory fields are always set
* builders include map and list builders
* targets **null-safe** usage (`java.util.Optional` used for optional fields and optional return values)
* especially concise usage with java `record` types
* generates builders for `class`, `record` and `interface` types
* supports JDK 8+, Maven, Gradle
* generates source (as well as compiled classes) so is IDE friendly as long as the IDE is *build-helper-maven-plugin* aware.

## Getting started

### Maven
Make these changes to your `pom.xml`:
* add *chained-api* artifact to the dependencies
* add the *chained-processor* annotation processor dependency to the *maven-compiler-plugin*
* configure *build-helper-maven-plugin* to ensure generated source is picked up in IDEs and the sources jar

```xml
<dependencies>
    ...
    <dependency>
        <groupId>com.github.davidmoten</groupId>
        <artifactId>chained-api</artifactId>
        <version>VERSION_HERE</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.14.0</version>
            <configuration>
                <generatedSourcesDirectory>
                    ${project.build.directory}/generated-sources/annotations</generatedSourcesDirectory>
                <generatedTestSourcesDirectory>
                       ${project.build.directory}/generated-test-sources/test-annotations</generatedTestSourcesDirectory>
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.github.davidmoten</groupId>
                        <artifactId>chained-processor</artifactId>
                        <version>VERSION_HERE</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>build-helper-maven-plugin</artifactId>
            <version>3.6.0</version> 
            <executions>
                <execution>
                    <id>add-source</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>add-source</goal>
                    </goals>
                    <configuration>
                        <sources>
                            <source>
                                ${project.build.directory}/generated-sources/annotations</source>
                        </sources>
                    </configuration>
                </execution>
                <!-- if you use the @Builder annotation in test classes as well then include this execution -->
                <execution>
                    <id>add-test-source</id>
                    <phase>generate-test-sources</phase>
                    <goals>
                        <goal>add-test-source</goal>
                    </goals>
                    <configuration>
                        <sources>
                            <source>
                                ${project.build.directory}/generated-test-sources/test-annotations</source>
                        </sources>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
### Gradle
This is how to use *chained* annotation processor in a gradle project (see [here](https://github.com/davidmoten/chained/tree/master/chained-gradle) for a demo minimal project):

**build.gradle** (tested with gradle 8.12.1):
```groovy
plugins {
    id 'java'
}

tasks.register('sourcesJar', Jar) {
    archiveClassifier.set('sources')
    from sourceSets.main.allSource
    from("$buildDir/generated/sources/annotationProcessor/java/main")
    dependsOn tasks.named('compileJava') 
}

artifacts {
    archives tasks.named('sourcesJar')
}

dependencies {
    implementation 'com.github.davidmoten:chained-api:VERSION_HERE'
    annotationProcessor 'com.github.davidmoten:chained-processor:VERSION_HERE'
}
```

## How to build

`mvn clean install`

or 

`gradle clean publishToMavenLocal`

## Examples

### Record types
java `record` types are a big boost in concise coding and offer more flexibility than generation from `interface` types. Let's create a builder for the `Person` class below using *chained*:

```java
package mine;

import com.github.davidmoten.chained.api.annotation.Builder;

@Builder
public final record Person(String name, int yearOfBirth, Optional<String> comments) {}
```
Using defaults this will generate the class `mine.builder.PersonBuilder`.

We can use it like this:

```java
Person p = PersonBuilder
    .builder()
    .name("Helen")
    .yearOfBirth(2001)
    .comments("enjoyed the event")
    .build();
```
From a discoverability perspective this is not great because a user has to know of the existence of `PersonBuilder`. Let's improve this by adding a static builder method to `Person` class:

```java
package mine;

import com.github.davidmoten.chained.api.annotation.Builder;
import mine.builder.PersonBuilder.BuilderWithName;

@Builder
public final record Person(String name, int yearOfBirth, Optional<String> comments) {
    public static BuilderWithName name(String name) {
        return PersonBuilder.builder().name(name);
    }
}
```
Now we create a `Person` like this:
```java
Person p = Person
    .name("Helen")
    .yearOfBirth(2001)
    .comments("enjoyed the event")
    .build();
```
We've knocked out the `.builder()` call (`name` is mandatory so always has to be specified, forced at compile time), and we have discoverability back (because the creation of `Person` is via a factory method on the `Person` class. 

It's very convenient for us that the annotation processor being run by the maven compiler plugin can do this. `javac` hands java structures parsed from source to the annotation processor and doesn't check that all references to classes actually exist till multi-round annotation processing has finished.

Note that the `comments` field is optional and these are our creation options in the builder due to the existence of method overrides:

```java
// leave comments out
Person a = Person
    .name("Helen")
    .yearOfBirth(2001)
    .build();

// pass empty comments
Person a = Person
    .name("Helen")
    .yearOfBirth(2001)
    .comments(Optional.empty())
    .build();

// pass comments
Person a = Person
    .name("Helen")
    .yearOfBirth(2001)
    .comments("cool person")
    .build();

// pass wrapped comments
Person a = Person
    .name("Helen")
    .yearOfBirth(2001)
    .comments(Optional.of("cool person"))
    .build();
```

### Providing the full class name of the generated classes

Set the value of the `@Builder` annotation to customize the full generated class name. The value can be templated with these items:
* `${pkg}` - the package of the class with the `@Builder` annotation
* `${simpleName}` - the simple name of the class with the `@Builder` annotation
The default value is `${pkg}.builder.${simpleName}Builder`.

For example if you want the generated builder class to be in the same package:
```java
package mine;

import com.github.davidmoten.chained.api.annotation.Builder;

@Builder("${pkg}.${simpleName}Builder")
public final record Person(String name, int yearOfBirth, Optional<String> comments) {}
```

### Generating from interface types
This generation method is especially useful for JDK < 17 (when `record` type was introduced).

```java
package mine;

import com.github.davidmoten.chained.api.annotation.Builder;
import mine.builder.PersonBuilder.BuilderWithName;

@Builder
public interface Person {

    String name();
    int yearOfBirth();
    Optional<String> description();
    
    public static BuilderWithName name(String name) {
        return PersonBuilder.builder().name(name);
    }
}
```
This generation method generates `PersonBuilder` class and also a `PersonImpl` class next to it. The returned instance of `Person` from the builder is actually an instance of `PersonImpl`.

If you want to validate the fields add a `default` method annotated with `@Check`:

```java
package mine;

import com.github.davidmoten.chained.api.Preconditions;
import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.api.annotation.Check;
import mine.builder.PersonBuilder.BuilderWithName;

@Builder
public interface Person {

    String name();
    int yearOfBirth();
    Optional<String> description();
    
    @Check
    default void check() {
        Preconditions.checkArgument(name().trim().length() > 0, "name cannot be blank");
        Preconditions.checkArgument(yearOfBirth() > 1900, "yearOfBirth must be after 1900");
        Preconditions.checkArgument(
            description()
                .map(x -> x.length)
                .orElse(0) < 4096,
            "description must be less than 4096 characters");
    }
    
    public static BuilderWithName name(String name) {
        return PersonBuilder.builder().name(name);
    }
}

```

### Generating from class types

TODO

### Map builders

Fields that are declared with the types below will have corresponding map builders:
* `java.util.Map`
* `java.util.HashMap`
* `java.util.SortedMap`
* `java.util.NavigableMap`
* `java.util.TreeMap`
* `java.util.LinkedHashMap`

For example, given this class:
```java
@Builder
public record HasProperties(String name, Map<String, String> properties) {
    public static HasProperties name(String name) {
        return HasPropertiesBuilder.builder().name();
    }
}
```
we can assign the map in one go:

```java
Map<String, String> properties = Map.of("scars", "true", "yearOfBirth", "2000");
HasProperties a = HasProperties
    .name("jack")
    .properties(properties);
```

or more fluidly:

```java
HasProperties a = HasProperties
    .name("jack") 
    .properties()
    .put("scars", "true")
    .put("yearOfBirth", "2000")
    .buildMap();
```

### List builders

Fields that are declared with the types below will have corresponding list builders:
* `java.util.List`
* `java.util.ArrayList`
* `java.util.LinkedList`

For example, given this class:

```java
@Builder
public record HasNumbers(String name, List<T> numbers) {
    public static HasProperties name(String name) {
        return HasPropertiesBuilder.builder().name();
    }
}
```
we can assign the list in one go:

```java
List<Integer> numbers = List.of(1, 2, 3);
HasNumbers a = HasNumbers
    .name("jack")
    .numbers(numbers);
```

or more fluidly:

```java
HasNumbers a = HasNumbers
    .name("jack") 
    .numbers()
    .add(1, 2)
    .add(3)
    .buildList();
```

### Set builders

Fields that are declared with the types below will have corresponding set builders:
* `java.util.Set`
* `java.util.SortedSet`
* `java.util.TreeSet`

For example, given this class:
```java
@Builder
public record HasNumbers(String name, Set<T> numbers) {
    public static HasProperties name(String name) {
        return HasPropertiesBuilder.builder().name();
    }
}
```
we can assign the set in one go:

```java
Set<Integer> numbers = Set.of(1, 2, 3);
HasNumbers a = HasNumbers
    .name("jack")
    .numbers(numbers);
```

or more fluidly:

```java
HasNumbers a = HasNumbers
    .name("jack") 
    .numbers()
    .add(1, 2)
    .add(3)
    .buildList();
```

### Modelling patterns
These are some aspects of modelling that you may want to represent:
* field constraints (like OpenAPI `min`, `max`, `minLength`, `maxLength`, `pattern`)
* field defaults (when a value is not specified)
* field transformations

### How to implement field constraints
Use the constructor for `record` and `class` types, and the `@Check` annotation for `interface` types.

An example with a `record` type:

```java
public Person(String name, int yearOfBirth) {
    public Person {
        Preconditions.checkArgument(yearOfBirth >= 1900, "yearOfBirth must be >= 1900");
    }
}
```
See [Generating from interface types](#generating-from-interface-types) for an example of using `@Check`.

#### How to implement field defaults and transformations
Modify field inputs in the constructor for `record` and `class` types, not available for `interface` types (but there are workarounds).

A field default example with a `record` type:

```java
public Order(String id, Optional<Integer> number) {
    public Order {
        if (number.isEmpty()) {
            number = Optional.of(1);
        }
    }
}
```

This is also achievable with the `interface` type but not as cleanly because we have exposed two `number` accessors in the public API:

```java
public interface Order {
    String id();
    
    Optional<Integer> numberInput();
    
    default int number() {
        return numberInput().orElse(1);
    }
}
```
