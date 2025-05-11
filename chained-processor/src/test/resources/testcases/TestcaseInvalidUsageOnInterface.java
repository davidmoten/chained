package com.github.davidmoten.chained.processor.tests;

import com.github.davidmoten.chained.api.Builder;

@Builder("${pkg}.Xyz")
public interface TestcaseInvalidUsageOnInterface {

    String doSomething();

}