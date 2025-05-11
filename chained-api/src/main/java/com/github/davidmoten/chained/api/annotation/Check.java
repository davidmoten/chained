package com.github.davidmoten.chained.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method performs a check on the state of the
 * object or its parameters. This annotation is typically used to mark methods
 * that validate conditions, enforce invariants, or perform assertions.
 * 
 * <p>{@code @Check} annotated methods
 * <ul>
 * <li>should not have side effects and are expected to throw exceptions if the
 * checks fail</li>
 * <li>are often used in conjunction with preconditions, postconditions, or
 * invariants to ensure the correctness of the code</li>
 * <li>can be used for debugging, testing, or runtime validation</li>
 * <li>should be well-documented to explain the checks being performed and the
 * expected behavior when the checks fail</li>
 * <li>can be used in various contexts, such as input validation, state
 * validation, or consistency checks</li>
 * </ul>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value = { ElementType.METHOD })
@Documented
public @interface Check {

}
