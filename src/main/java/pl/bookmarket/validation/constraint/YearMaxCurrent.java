package pl.bookmarket.validation.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;

/**
 * Checks if the annotated integer representing year is not greater than the current year, obtained from
 * {@link LocalDate#getYear()} method using default time-zone.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = YearMaxCurrentValidator.class)
public @interface YearMaxCurrent {

    String message() default "value.exceeded";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}