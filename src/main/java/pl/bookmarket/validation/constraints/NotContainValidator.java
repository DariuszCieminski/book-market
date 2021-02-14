package pl.bookmarket.validation.constraints;

import java.util.Arrays;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotContainValidator implements ConstraintValidator<NotContain, String> {

    private String[] forbiddenValues;

    @Override
    public void initialize(NotContain constraintAnnotation) {
        this.forbiddenValues = constraintAnnotation.values();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return Arrays.stream(forbiddenValues)
                     .noneMatch(forbiddenValue -> value.toLowerCase().contains(forbiddenValue.toLowerCase()));
    }
}