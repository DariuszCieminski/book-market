package pl.bookmarket.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Validation;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.bookmarket.model.User;
import pl.bookmarket.dto.ChangePasswordDto;
import pl.bookmarket.service.crud.UserService;

public class ChangePasswordValidator implements ConstraintValidator<ChangePassword, ChangePasswordDto> {

    private final UserService userService;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ChangePasswordValidator(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public boolean isValid(ChangePasswordDto value, ConstraintValidatorContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByLogin(authentication.getName());

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