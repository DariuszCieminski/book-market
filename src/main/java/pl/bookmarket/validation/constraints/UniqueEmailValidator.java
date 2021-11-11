package pl.bookmarket.validation.constraints;

import pl.bookmarket.service.crud.UserService;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    private final UserService userService;

    public UniqueEmailValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            userService.getUserByEmail(value);
            return false;
        } catch (EntityNotFoundException exception) {
            return true;
        }
    }
}