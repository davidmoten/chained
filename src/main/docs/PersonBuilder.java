// GENERATED FILE - DO NOT EDIT
package mine.builder;

import com.github.davidmoten.chained.api.Helpers;
import com.github.davidmoten.chained.api.Preconditions;
import com.github.davidmoten.chained.unittest.Person;
import jakarta.annotation.Generated;
import jakarta.annotation.Nonnull;
import java.lang.String;
import java.util.Optional;

@Generated("com.github.davidmoten:chained-processor")
public final class PersonBuilder {

    private String name;
    private int yearOfBirth;
    private Optional<String> comments = Optional.empty();

    private PersonBuilder() {
        // prevent instantiation
    }

    public static PersonBuilder builder() {
        return new PersonBuilder();
    }

    public static PersonBuilder create() {
        return builder();
    }

    /**
     * Sets {@code name}.
     * 
     * @param name the value to assign
     * @return builder
     */
    public BuilderWithName name(@Nonnull String name) {
        Preconditions.checkNotNull(name, "name");
        this.name = name;
        return new BuilderWithName(this);
    }

    private Person build() {
        return new Person(
                Helpers.unmodifiable(name),
                Helpers.unmodifiable(yearOfBirth),
                Helpers.unmodifiable(comments));
    }

    public final static class BuilderWithName {

        private final PersonBuilder _b;

        private BuilderWithName(PersonBuilder _b) {
            this._b = _b;
        }

        /**
         * Sets {@code yearOfBirth}.
         * 
         * @param yearOfBirth the value to assign
         * @return builder
         */
        public BuilderWithYearOfBirth yearOfBirth(@Nonnull int yearOfBirth) {
            _b.yearOfBirth = yearOfBirth;
            return new BuilderWithYearOfBirth(_b);
        }
    }

    public final static class BuilderWithYearOfBirth {

        private final PersonBuilder _b;

        private BuilderWithYearOfBirth(PersonBuilder _b) {
            this._b = _b;
        }

        /**
         * Sets {@code comments}. This parameter is <b>OPTIONAL</b>, the call can be
         * omitted or an overload can be called with {@code Optional.empty()}.
         * 
         * @param comments the value to assign
         * @return builder
         */
        public BuilderWithYearOfBirth comments(@Nonnull String comments) {
            Preconditions.checkNotNull(comments, "comments");
            this._b.comments = Optional.of(comments);
            return this;
        }

        /**
         * Sets {@code comments}. This parameter is <b>OPTIONAL</b>, the call can be
         * omitted or this method can be called with {@code Optional.empty()}.
         * 
         * @param comments the value to assign
         * @return builder
         */
        public BuilderWithYearOfBirth comments(@Nonnull Optional<String> comments) {
            Preconditions.checkNotNull(comments, "comments");
            _b.comments = comments;
            return this;
        }

        public Person build() {
            return _b.build();
        }
    }

    public static CopyBuilder copy(@Nonnull Person value) {
        return new CopyBuilder(value);
    }

    public static final class CopyBuilder {

        private String name;
        private int yearOfBirth;
        private Optional<String> comments;

        private CopyBuilder(Person value) {
            this.name = value.name();
            this.yearOfBirth = value.yearOfBirth();
            this.comments = value.comments();
        }

        /**
         * Sets {@code name}.
         * 
         * @param name the value to assign
         * @return builder
         */
        public CopyBuilder name(@Nonnull String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets {@code yearOfBirth}.
         * 
         * @param yearOfBirth the value to assign
         * @return builder
         */
        public CopyBuilder yearOfBirth(@Nonnull int yearOfBirth) {
            this.yearOfBirth = yearOfBirth;
            return this;
        }

        /**
         * Sets {@code comments}. This parameter is <b>OPTIONAL</b>, the call can be
         * omitted or an overload can be called with {@code Optional.empty()}.
         * 
         * @param comments the value to assign
         * @return builder
         */
        public CopyBuilder comments(@Nonnull String comments) {
            this.comments = Optional.of(comments);
            return this;
        }

        /**
         * Sets {@code comments}. This parameter is <b>OPTIONAL</b>, the call can be
         * omitted or this method can be called with {@code Optional.empty()}.
         * 
         * @param comments the value to assign
         * @return builder
         */
        public CopyBuilder comments(@Nonnull Optional<String> comments) {
            this.comments = comments;
            return this;
        }

        public Person build() {
            return new Person(this.name, this.yearOfBirth, this.comments);
        }
    }
}
