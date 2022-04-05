package pl.bookmarket.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class NotContainValidator implements ConstraintValidator<NotContain, String> {

    private String[] forbiddenValues;

    @Override
    public void initialize(NotContain constraintAnnotation) {
        this.forbiddenValues = constraintAnnotation.values();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return Arrays.stream(forbiddenValues).noneMatch(value.toLowerCase()::contains);
    }
}