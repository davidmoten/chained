package thing.Demo;

import com.github.davidmoten.chained.api.annotation.Builder;

@Builder
public record Demo(String name, int yearOfBirth) {}