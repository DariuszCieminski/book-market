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
import pl.bookmarket.util.ChangePasswordModel;

public class ChangePasswordValidator implements ConstraintValidator<ChangePassword, ChangePasswordModel> {

    private final UserDao userDAO;

    @Autowired
    public ChangePasswordValidator(UserDao userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public boolean isValid(ChangePasswordModel value, ConstraintValidatorContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        User user = userDAO.findUserByLogin(authentication.getName());

        if (user == null) {
            return false;
        }

        if (!BCrypt.checkpw(value.getOldPassword(), user.getPassword())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{password.invalid}").addConstraintViolation();
            return false;
        }

        if (!validator.validateValue(User.class, "rawPassword", value.getNewPassword()).isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{password.not.match.regex}").addConstraintViolation();
            return false;
        }

        if (!value.getNewPassword().equals(value.getConfirmNewPassword())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{passwords.dont.match}").addConstraintViolation();
            return false;
        }

        return true;
    }
}