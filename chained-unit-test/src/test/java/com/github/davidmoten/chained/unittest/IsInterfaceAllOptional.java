package com.github.davidmoten.chained.unittest;

import java.util.Optional;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.IsInterfaceAllOptionalBuilder;

@Builder
public interface IsInterfaceAllOptional {
    
    Optional<String> name();
    
    Optional<Integer> yearOfBirth();
    
    Optional<String> description();
    
    public static IsInterfaceAllOptionalBuilder builder() {
        return IsInterfaceAllOptionalBuilder.builder();
    }

}
