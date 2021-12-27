package pl.bookmarket.validation.constraints;

import org.springframework.beans.factory.annotation.Autowired;
import pl.bookmarket.dao.RoleDao;
import pl.bookmarket.dto.RoleDto;
import pl.bookmarket.model.Role;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueRoleValidator implements ConstraintValidator<UniqueRole, RoleDto> {

    private final RoleDao roleDAO;

    @Autowired
    public UniqueRoleValidator(RoleDao roleDAO) {
        this.roleDAO = roleDAO;
    }

    @Override
    public boolean isValid(RoleDto value, ConstraintValidatorContext context) {
        Optional<Role> role = roleDAO.findRoleByName(value.getName());

        boolean valid = (!role.isPresent() || role.get().getId().equals(value.getId()));

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("name.occupied")
                   .addPropertyNode("name")
                   .addConstraintViolation();
        }

        return valid;
    }
}