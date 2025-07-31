package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.annotation.Builder;

/**
 * Example record with Javadoc.
 * 
 * @param name the name of the person
 * @param age the age of the person 
 */
@Builder
public record RecordWithJavadoc(String name, int age, int heightCm) {
}
