package pl.bookmarket.service.crud;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.RoleDao;
import pl.bookmarket.model.Role;
import pl.bookmarket.validation.exception.EntityNotFoundException;
import pl.bookmarket.validation.exception.EntityValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class RoleServiceImpl implements RoleService {
    private final RoleDao roleDao;

    public RoleServiceImpl(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    @Override
    public List<Role> getAllRoles() {
        List<Role> roleList = new ArrayList<>();
        roleDao.findAll().forEach(roleList::add);
        return roleList;
    }

    @Override
    public Optional<Role> getRoleById(Long id) {
        return roleDao.findById(id);
    }

    @Override
    public Role createRole(Role role) {
        if (roleDao.existsRoleByName(role.getName())) {
            throw new EntityValidationException("name", "name.occupied");
        }
        return roleDao.save(role);
    }

    @Override
    public Role updateRole(Role role) {
        Role byId = roleDao.findById(role.getId()).orElseThrow(() -> new EntityNotFoundException(Role.class));
        if (roleDao.existsRoleByName(role.getName()) && !byId.getName().equals(role.getName())) {
            throw new EntityValidationException("name", "name.occupied");
        }
        return roleDao.save(role);
    }

    @Override
    public void deleteRole(Long id) {
        if (!roleDao.existsById(id)) {
            throw new EntityNotFoundException(Role.class);
        }
        roleDao.deleteById(id);
    }
}