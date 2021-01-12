package pl.bookmarket.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import pl.bookmarket.model.Role;

public interface RoleDao extends CrudRepository<Role, Long> {

    @EntityGraph(attributePaths = {"users", "users.books"})
    Role findRoleByName(String name);

    @Override
    @EntityGraph(attributePaths = {"users", "users.books", "users.books.genre"})
    Iterable<Role> findAll();
}