package pl.bookmarket.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueGenreValidator.class)
public @interface UniqueGenre {

    String message() default "name.occupied";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}