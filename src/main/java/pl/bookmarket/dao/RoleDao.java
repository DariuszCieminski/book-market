package pl.bookmarket.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.bookmarket.model.Role;

import java.util.Optional;

@Repository
public interface RoleDao extends CrudRepository<Role, Long> {

    Optional<Role> findRoleByName(String name);

    boolean existsRoleByName(String name);
}