package pl.bookmarket.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import pl.bookmarket.model.User;
import pl.bookmarket.dto.ResetPasswordDto;
import pl.bookmarket.service.crud.UserService;

public class ResetPasswordValidator implements ConstraintValidator<ResetPassword, ResetPasswordDto> {

    private final UserService userService;

    @Autowired
    public ResetPasswordValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(ResetPasswordDto value, ConstraintValidatorContext context) {
        User user = userService.getUserByLogin(value.getLogin());

        if (user == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{user.invalid}").addConstraintViolation();
            return false;
        }

        if (!user.getEmail().equals(value.getEmail())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{email.invalid}").addConstraintViolation();
            return false;
        }

        return true;
    }
}