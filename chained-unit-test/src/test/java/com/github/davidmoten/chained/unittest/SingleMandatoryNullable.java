package com.github.davidmoten.chained.unittest;

import javax.annotation.Nullable;

import com.github.davidmoten.chained.api.annotation.Builder;

@Builder
public record SingleMandatoryNullable(@Nullable String name) {

}
