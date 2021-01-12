package pl.bookmarket.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.User;
import pl.bookmarket.util.ChangeEmailModel;

public class ChangeEmailValidator implements ConstraintValidator<ChangeEmail, ChangeEmailModel> {

    private final UserDao userDao;

    @Autowired
    public ChangeEmailValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean isValid(ChangeEmailModel value, ConstraintValidatorContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        User user = userDao.findUserByLogin(authentication.getName());

        if (user == null) {
            return false;
        }

        if (!BCrypt.checkpw(value.getPassword(), user.getPassword())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{password.invalid}").addConstraintViolation();
            return false;
        }

        if (!validator.validateValue(User.class, "email", value.getNewEmail()).isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{email.invalid}").addConstraintViolation();
            return false;
        }

        if (!value.getNewEmail().equals(value.getConfirmNewEmail())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{emails.dont.match}").addConstraintViolation();
            return false;
        }

        boolean newEmailExists = userDao.findUserByEmail(value.getNewEmail()) != null;

        if (newEmailExists) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{email.occupied}").addConstraintViolation();
            return false;
        }

        return true;
    }
}