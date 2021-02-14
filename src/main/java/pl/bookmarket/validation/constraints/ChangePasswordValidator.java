package pl.bookmarket.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Validation;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.User;
import pl.bookmarket.util.ChangePasswordModel;

public class ChangePasswordValidator implements ConstraintValidator<ChangePassword, ChangePasswordModel> {

    private final UserDao userDAO;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ChangePasswordValidator(UserDao userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public boolean isValid(ChangePasswordModel value, ConstraintValidatorContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDAO.findUserByLogin(authentication.getName());

        if (user == null) {
            return false;
        }

        if (!passwordEncoder.matches(value.getOldPassword(), user.getPassword())) {
            return buildConstraintViolationWithMessage(context, "{password.invalid}");
        }

        if (!validator.validateValue(User.class, "password", value.getNewPassword()).isEmpty()) {
            return buildConstraintViolationWithMessage(context, "{password.not.match.regex}");
        }

        if (!value.getNewPassword().equals(value.getConfirmNewPassword())) {
            return buildConstraintViolationWithMessage(context, "{passwords.dont.match}");
        }

        return true;
    }

    private boolean buildConstraintViolationWithMessage(ConstraintValidatorContext context, String messageTemplate) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
        return false;
    }
}