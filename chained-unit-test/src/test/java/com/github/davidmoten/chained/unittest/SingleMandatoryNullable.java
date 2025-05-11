package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.annotation.Builder;

import jakarta.annotation.Nullable;

@Builder
public record SingleMandatoryNullable(@Nullable String name) {

}
