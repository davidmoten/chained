package com.github.davidmoten.chained.unittest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import com.github.davidmoten.chained.unittest.builder.SingleMandatoryBuilder;
import com.github.davidmoten.chained.unittest.builder.SingleOptionalBuilder;

public class ChainedProcessorTest {

    @Test
    public void testSingleMandatory() {
        SingleMandatory s = SingleMandatoryBuilder.of("thing");
        assertEquals("thing", s.name());
    }

    @Test
    public void testSingleOptional() {
        SingleOptional s = SingleOptionalBuilder.builder().name("thing").build();
        assertEquals("thing", s.name().get());
    }

    @Test
    public void testTwoMandatory() {
        TwoMandatory s = TwoMandatory.name("fred").age(10);
        assertEquals("fred", s.name());
        assertEquals(10, s.age());
    }

    @Test
    public void testMixed() {
        Mixed a = Mixed.name("fred").city("London").age(10).description("someone").build();
        assertEquals("fred", a.name());
        assertEquals("London", a.city());
        assertEquals("someone", a.description().get());
        assertEquals(10, (long) a.age().get());
    }

    @Test
    public void testPrivateConstructor() {
        PrivateConstructor a = PrivateConstructor.name("fred").city("Canberra");
        assertEquals("fred", a.name());
        assertEquals("Canberra", a.city());
    }

    @Test
    public void testDefaultConstructorSamePackageDoesNotUseReflection() throws IOException {
        DefaultConstructorSamePackage a = DefaultConstructorSamePackage.name("fred").city("Canberra");
        assertEquals("fred", a.name());
        assertEquals("Canberra", a.city());
        String code = Files.readString(new File(
                "target/generated-test-sources/test-annotations/com/github/davidmoten/chained/unittest/DefaultConstructorSamePackageBuilder.java")
                .toPath());
        assertFalse(code.contains("java.lang.reflect.Constructor"));
    }

    @Test
    public void testTwoMandatoryIncludeBuilderMethod() {
        TwoMandatoryIncludeBuilderMethod s = TwoMandatoryIncludeBuilderMethod.name("fred").age(10).build();
        assertEquals("fred", s.name());
        assertEquals(10, s.age());
    }

    @Test
    public void testTwoOptionalEmpty() {
        TwoOptional s = TwoOptional.builder().build();
        assertEquals(java.util.Optional.empty(), s.name());
        assertEquals(java.util.Optional.empty(), s.age());
    }

    @Test
    public void testTwoOptionalPresent() {
        TwoOptional s = TwoOptional.builder().name("hi there").age(Optional.of(12)).build();
        assertEquals("hi there", s.name().get());
        assertEquals(12, (int) s.age().get());
    }

    @Test
    public void testOfPrivateConstructorSamePackage() {
        OfPrivateConstructorSamePackage a = OfPrivateConstructorSamePackage.of("fred");
        assertEquals("fred", a.name());
    }

    @Test
    public void testHasMap() {
        HasMap a = HasMap.name("fred").map(Map.of("a", 1, "b", 2));
        HasMap.name("fred").map().put("a", 1).put("b", 2).buildMap().map(Collections.emptyMap());
    }

}
