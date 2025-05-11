package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.annotation.Builder;

/**
 * Example record with Javadoc.
 * 
 * @param name the name of the person
 * @param age the age of the person which should be rounded up to the nearest integer unless the person is deceased in which case it should be rounded down
 * @param apparentAge the apparent age of the person
 *        which should be rounded
 *        up to the nearest integer unless the 
 *        person is deceased in which case
 *        it should be rounded down
 */
@Builder
public record RecordWithJavadoc(String name, int age, int apparentAge, int heightCm) {
}
