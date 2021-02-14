package pl.bookmarket.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.User;

public class UniqueRegisterDataValidator implements ConstraintValidator<UniqueRegisterData, User> {

    private final UserDao userDao;

    @Autowired
    public UniqueRegisterDataValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean isValid(User value, ConstraintValidatorContext context) {
        boolean valid = true;

        User dbUser = userDao.findUserByLogin(value.getLogin());

        if (dbUser != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{login.occupied}")
                   .addPropertyNode("login")
                   .addConstraintViolation();
            valid = false;
        }

        dbUser = userDao.findUserByEmail(value.getEmail());

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