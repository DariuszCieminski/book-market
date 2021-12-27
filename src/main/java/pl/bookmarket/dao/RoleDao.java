package pl.bookmarket.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.bookmarket.model.Role;

import java.util.Optional;

@Repository
public interface RoleDao extends CrudRepository<Role, Long> {

    @EntityGraph(attributePaths = {"users", "users.books"})
    Optional<Role> findRoleByName(String name);

    @Override
    @EntityGraph(attributePaths = {"users", "users.books", "users.books.genre"})
    Iterable<Role> findAll();
}