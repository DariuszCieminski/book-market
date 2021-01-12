package pl.bookmarket.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotContainValidator implements ConstraintValidator<NotContain, String> {

    private String[] values;

    @Override
    public void initialize(NotContain constraintAnnotation) {
        this.values = constraintAnnotation.values();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        for (String forbidden : values) {
            if (value.toLowerCase().contains(forbidden.toLowerCase())) {
                return false;
            }
        }

        return true;
    }
}