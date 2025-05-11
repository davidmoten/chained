package com.github.davidmoten.chained.unittest;
import java.util.Optional;

import com.github.davidmoten.chained.api.annotation.Builder;

@Builder(value = "mine.builder.PersonBuilder")
public final record Person(String name, int yearOfBirth, Optional<String> comments) {}