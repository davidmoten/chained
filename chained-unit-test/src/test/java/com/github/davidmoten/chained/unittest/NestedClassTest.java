package com.github.davidmoten.chained.unittest;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.InsideBuilder;
import com.github.davidmoten.chained.unittest.builder.InsideBuilder.BuilderWithName;

public class NestedClassTest {

    @Builder
    public static record Inside(String name, int yearOfBirth) {
        public static BuilderWithName name(String name) {
            return InsideBuilder.builder().name(name);
        }
    }

    @Builder
    public class InsideInner {
        private final String name;
        private final int yearOfBirth;

        public InsideInner(String name, int yearOfBirth) {
            this.name = name;
            this.yearOfBirth = yearOfBirth;
        }

        public String name() {
            return name;
        }

        public int yearOfBirth() {
            return yearOfBirth;
        }
    }

    @Test
    public void testInsideInnerBuilderNotGenerated() throws ClassNotFoundException {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName(NestedClassTest.class.getName() + ".builder.InsideInnerBuilder");
        });
    }

}
