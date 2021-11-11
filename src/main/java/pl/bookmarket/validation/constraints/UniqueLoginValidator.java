package pl.bookmarket.validation.constraints;

import pl.bookmarket.service.crud.UserService;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueLoginValidator implements ConstraintValidator<UniqueLogin, String> {
    private final UserService userService;

    public UniqueLoginValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            userService.getUserByLogin(value);
            return false;
        } catch (EntityNotFoundException exception) {
            return true;
        }
    }
}