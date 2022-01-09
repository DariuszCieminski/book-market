package pl.bookmarket.service.crud;

import pl.bookmarket.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    List<Role> getAllRoles();

    Optional<Role> getRoleById(Long id);

    Role createRole(Role role);

    Role updateRole(Role role);

    void deleteRole(Long id);
}