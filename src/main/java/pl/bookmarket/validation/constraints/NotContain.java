package pl.bookmarket.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated CharSequence must not contain any sequence of char values in the specified String array.
 * <p>
 * Accepts {@code CharSequence}. {@code null} elements are considered valid.
 *
 * @see String#contains(CharSequence)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotContainValidator.class)
public @interface NotContain {

    String[] values();

    String message() default "contains.forbidden.value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}