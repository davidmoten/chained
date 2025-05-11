package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.annotation.Builder;

@Builder
public final record SingleMandatory(String name) {
    
}
