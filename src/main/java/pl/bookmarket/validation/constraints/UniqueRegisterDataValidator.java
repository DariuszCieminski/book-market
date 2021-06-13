package pl.bookmarket.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import pl.bookmarket.model.User;
import pl.bookmarket.service.crud.UserService;

public class UniqueRegisterDataValidator implements ConstraintValidator<UniqueRegisterData, User> {

    private final UserService userService;

    @Autowired
    public UniqueRegisterDataValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(User value, ConstraintValidatorContext context) {
        boolean valid = true;

        User dbUser = userService.getUserByLogin(value.getLogin());

        if (dbUser != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{login.occupied}")
                   .addPropertyNode("login")
                   .addConstraintViolation();
            valid = false;
        }

        dbUser = userService.getUserByEmail(value.getEmail());

        if (dbUser != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{email.occupied}")
                   .addPropertyNode("email")
                   .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}