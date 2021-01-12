package pl.bookmarket.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.User;
import pl.bookmarket.util.ResetPasswordModel;

public class ResetPasswordValidator implements ConstraintValidator<ResetPassword, ResetPasswordModel> {

    private final UserDao userDao;

    @Autowired
    public ResetPasswordValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean isValid(ResetPasswordModel value, ConstraintValidatorContext context) {
        User user = userDao.findUserByLogin(value.getLogin());

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