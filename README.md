# chained
Java annotation processor to generate chained immutable builders including map and list builders.
* chained builders provide compile-time *type-safety* to ensure mandatory fields are always set
* builders include map and list builders
* targets **null-safe** usage (`java.util.Optional` used for optional fields and optional return values)
* especially concise usage with java `record` types
* generates builders for `class`, `record` and `interface` types
* supports JDK 8+, Maven, Gradle

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
                        <version>${project.parent.version}</version>
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
                        <phase>generate-sources</phase>
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

**build.gradle** (using gradle 8.12.1):
```groovy
plugins {
    id 'java'
}

java {
    sourceCompatibility = JavaVersion.VERSION_17  // adjust if needed
    targetCompatibility = JavaVersion.VERSION_17
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

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'com.github.davidmoten:chained-api:VERSION_HERE'
    annotationProcessor 'com.github.davidmoten:chained-processor:VERSION_HERE'
}
```

## How to build

`mvn clean install`

## Examples
java `record` types are a big boost in concise coding. Let's create a builder for the `Person` class below using *chained*:

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

It's very convenient for us that the annotation processor being run by the maven compiler plugin can do this. `javac` passes java structures parsed from source to the annotation processor and doesn't check that all references to classes actually exist till multi-round annotation processing has finished.

Note that the `comments` field is optional so we can also create a `Person` as below:

```java
Person a = Person
    .name("Helen")
    .yearOfBirth(2001)
    .build();

Optional<String> comments = Optional.of("enjoyed the event");
Person a = Person
    .name("Helen")
    .yearOfBirth(2001)
    .comments(comments)
    .build();
```



