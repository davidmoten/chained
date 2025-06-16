package com.github.davidmoten.chained.unittest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    public void testHasMapAllMandatory() {
        Map<String, Integer> map = Map.of("a", 1, "b", 2);
        HasMap a = HasMap.name("fred").map(map);
        assertEquals("fred", a.name());
        assertEquals(map, a.map());
        HasMap b = HasMap.name("fred").map().put("a", 1).put("b", 2).buildMap();
        assertEquals(a, b);
    }

    @Test
    public void testHasListAllMandatory() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        HasList a = HasList.name("julia").list(list);
        assertEquals("julia", a.name());
        assertEquals(list, a.list());
        List<Integer> more = Arrays.asList(2, 3);
        HasList b = HasList.name("julia").list().add(1).addAll(more).buildList();
        assertEquals(a, b);
    }

    @Test
    public void testIsInterfaceAllOptional() {
        IsInterfaceAllOptional a = IsInterfaceAllOptional.builder().name("fred").build();
        assertEquals("fred", a.name().get());
        assertFalse(a.yearOfBirth().isPresent());
        assertFalse(a.description().isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsInterfaceCheck() {
        IsInterface.builder().name("anne").yearOfBirth(0).build();
    }

    @Test
    public void testTransformFields() {
        TransformFields a = TransformFields.a(-1).b(-2);
        assertEquals(0, a.a());
        assertEquals(2, a.b());
    }

    @Test
    public void testHasSet() {
        HasSet a = HasSet.name("woo").numbers().add(1, 2, 3).buildList();
        assertEquals("woo", a.name());
        assertEquals(Set.of(1, 2, 3), a.numbers());
    }

    @Test
    public void testOptionalAsNullable() {
        {
            OptionalAsNullable a = OptionalAsNullable.name("fred").build();
            assertEquals("fred", a.name());
            assertNull(a.age());
        }
        {
            OptionalAsNullable a = OptionalAsNullable.name("fred").age(10).build();
            assertEquals("fred", a.name());
            assertEquals(10, (int) a.age());
        }
        {
            OptionalAsNullable a = OptionalAsNullable.name("fred").age(null).build();
            assertEquals("fred", a.name());
            assertNull(a.age());
        }
    }

    @Test
    public void testTwoNullable() {
        {
            TwoNullable a = TwoNullable.builder().name("fred").age(10).build();
            assertEquals("fred", a.name());
            assertEquals(10, (int) a.age());
        }
        {
            TwoNullable a = TwoNullable.builder().name("fred").age(null).build();
            assertEquals("fred", a.name());
            assertNull(a.age());
        }
        {
            TwoNullable a = TwoNullable.builder().name(null).age(null).build();
            assertNull(a.name());
            assertNull(a.age());
        }
        {
            TwoNullable a = TwoNullable.builder().build();
            assertNull(a.name());
            assertNull(a.age());
        }
    }

    @Test
    public void testNullableMap() {
        {
            NullableMap a = NullableMap.name("fred").map(Map.of("a", 1, "b", 2)).build();
            assertEquals("fred", a.name());
            assertEquals(Map.of("a", 1, "b", 2), a.map());
        }
        {
            NullableMap a = NullableMap.name("fred").map(null).build();
            assertEquals("fred", a.name());
            assertNull(a.map());
        }

        {
            NullableMap a = NullableMap.name("fred").build();
            assertEquals("fred", a.name());
            assertNull(a.map());
        }
    }

    @Test
    public void testCopy1() {
        Mixed a = Mixed.name("fred").city("London").age(10).description("someone").build();
        Mixed b = Mixed.copy(a).city("Paris").build();
        assertEquals("fred", b.name());
        assertEquals("Paris", b.city());
        assertEquals("someone", b.description().get());
        assertEquals(10, (long) b.age().get());
    }

    @Test
    public void testTwoOptional() {
        TwoOptional a = TwoOptional.builder().name("fred").age(10).build();
        TwoOptional b = a.copy().name("julia").build();
        assertEquals("julia", b.name().get());
        assertEquals(10, (int) b.age().get());
    }
}
