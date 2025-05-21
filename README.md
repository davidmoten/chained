# chained
Java annotation processor to generate chained immutable builders including map and list builders.
* targets **null-safe** usage (`java.util.Optional` used for parameters as well as return values)
* especially concise usage with java `record` types
* generates builders for `class`, `record` and `interface` types
* supports JDK 8+, Maven

## Getting started
Make these changes to your `pom.xml`:
* add *chained-api* artifact to the dependencies
* add the *chained-processor* annotation processor dependency to the *maven-compiler-plugin*
* configure *build-helper-maven-plugin* to ensure generated source is picked up in IDEs and the sources jar

```xml`
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

## How to build

`mvn clean install`


