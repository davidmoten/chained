package com.github.davidmoten.chained.unittest;

import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.chained.api.annotation.Builder;

@Builder
public record OptionalWithMap(Optional<String> name, Optional<Integer> age, Map<String, Integer> map) {
}
