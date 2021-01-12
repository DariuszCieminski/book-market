package pl.bookmarket.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import pl.bookmarket.dao.RoleDao;
import pl.bookmarket.model.Role;

public class UniqueRoleValidator implements ConstraintValidator<UniqueRole, Role> {

    private final RoleDao roleDAO;

    @Autowired
    public UniqueRoleValidator(RoleDao roleDAO) {
        this.roleDAO = roleDAO;
    }

    @Override
    public boolean isValid(Role value, ConstraintValidatorContext context) {
        Role dbRole = roleDAO.findRoleByName(value.getName());

        boolean valid = (dbRole == null || dbRole.getId().equals(value.getId()));

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{role.name.occupied}")
                   .addPropertyNode("name")
                   .addConstraintViolation();
        }

        return valid;
    }
}