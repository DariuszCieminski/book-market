package pl.bookmarket.validation.constraints;

import org.springframework.beans.factory.annotation.Autowired;
import pl.bookmarket.dto.ResetPasswordDto;
import pl.bookmarket.model.User;
import pl.bookmarket.service.crud.UserService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class ResetPasswordValidator implements ConstraintValidator<ResetPassword, ResetPasswordDto> {

    private final UserService userService;

    @Autowired
    public ResetPasswordValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(ResetPasswordDto value, ConstraintValidatorContext context) {
        Optional<User> user = userService.getUserByLogin(value.getLogin());

        if (!user.isPresent()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{user.invalid}").addConstraintViolation();
            return false;
        }

        if (!user.get().getEmail().equals(value.getEmail())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{email.invalid}").addConstraintViolation();
            return false;
        }

        return true;
    }
}