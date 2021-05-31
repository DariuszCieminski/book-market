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
import pl.bookmarket.dto.ChangeEmailDto;

public class ChangeEmailValidator implements ConstraintValidator<ChangeEmail, ChangeEmailDto> {

    private final UserDao userDao;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ChangeEmailValidator(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public boolean isValid(ChangeEmailDto value, ConstraintValidatorContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDao.findUserByLogin(authentication.getName());

        if (user == null) {
            return false;
        }

        if (!passwordEncoder.matches(value.getPassword(), user.getPassword())) {
            return buildConstraintViolationWithMessage(context, "{password.invalid}");
        }

        if (!validator.validateValue(User.class, "email", value.getNewEmail()).isEmpty()) {
            return buildConstraintViolationWithMessage(context, "{email.invalid}");
        }

        if (!value.getNewEmail().equals(value.getConfirmNewEmail())) {
            return buildConstraintViolationWithMessage(context, "{emails.dont.match}");
        }

        boolean isNewEmailAlreadyExisting = userDao.findUserByEmail(value.getNewEmail()) != null;

        if (isNewEmailAlreadyExisting) {
            return buildConstraintViolationWithMessage(context, "{email.occupied}");
        }

        return true;
    }

    private boolean buildConstraintViolationWithMessage(ConstraintValidatorContext context, String messageTemplate) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
        return false;
    }
}