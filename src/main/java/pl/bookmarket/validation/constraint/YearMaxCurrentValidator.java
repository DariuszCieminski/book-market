package pl.bookmarket.validation.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.ZoneId;

public class YearMaxCurrentValidator implements ConstraintValidator<YearMaxCurrent, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) return true;
        int currentYear = LocalDate.now(ZoneId.systemDefault()).getYear();
        return value <= currentYear;
    }
}