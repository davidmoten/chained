package com.github.davidmoten.chained.processor;

import io.toolisticon.aptk.tools.MessagerUtils;
import io.toolisticon.aptk.tools.corematcher.CoreMatcherValidationMessages;
import io.toolisticon.cute.Cute;
import io.toolisticon.cute.CuteApi;
import org.junit.Before;
import org.junit.Test;



/**
 * Tests of {@link com.github.davidmoten.chained.api.Builder}.
 *
 * TODO: replace the example testcases with your own testcases
 */

public class BuilderProcessorTest {


    CuteApi.BlackBoxTestSourceFilesInterface compileTestBuilder;

    @Before
    public void init() {
        MessagerUtils.setPrintMessageCodes(true);

        compileTestBuilder = Cute
                .blackBoxTest()
                .given()
                .processors(BuilderProcessor.class);
    }


    @Test
    public void test_valid_usage() {

        compileTestBuilder
                .andSourceFiles("testcases/TestcaseValidUsage.java")
                .whenCompiled()
                .thenExpectThat().compilationSucceeds()
                .executeTest();
    }

    @Test
    public void test_invalid_usage_with_empty_value() {

        compileTestBuilder
                .andSourceFiles("testcases/TestcaseInvalidUsageWithEmptyValue.java")
                .whenCompiled()
                .thenExpectThat().compilationFails()
                .andThat().compilerMessage().ofKindError().contains(BuilderProcessorCompilerMessages.ERROR_VALUE_MUST_NOT_BE_EMPTY.getCode())
                .executeTest();
    }

    @Test
    public void test_invalid_usage_on_enum() {

        compileTestBuilder
                .andSourceFiles("testcases/TestcaseInvalidUsageOnEnum.java")
                .whenCompiled()
                .thenExpectThat().compilationFails()
                .andThat().compilerMessage().ofKindError().contains(CoreMatcherValidationMessages.IS_CLASS.getCode())
                .executeTest();
    }

    @Test
    public void test_Test_invalid_usage_on_interface() {

        compileTestBuilder
                .andSourceFiles("testcases/TestcaseInvalidUsageOnInterface.java")
                .whenCompiled()
                .thenExpectThat().compilationFails()
                .andThat().compilerMessage().ofKindError().contains(CoreMatcherValidationMessages.IS_CLASS.getCode())
                .executeTest();
    }



}