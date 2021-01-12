package pl.bookmarket.dao;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.bookmarket.model.User;

public interface UserDao extends CrudRepository<User, Long> {

    @EntityGraph(attributePaths = {"roles", "books", "books.genre", "offers"})
    User findUserByLogin(String login);

    @EntityGraph(attributePaths = {"roles", "books"})
    User findUserByEmail(String email);

    @Query("select login from User where login<>?1")
    List<String> getUserLogins(String login);

    @Override
    @EntityGraph(attributePaths = {"roles", "books", "books.genre"})
    Iterable<User> findAll();

    @Override
    @EntityGraph(attributePaths = {"roles", "books", "books.genre"})
    Optional<User> findById(Long aLong);

    @Transactional
    @Modifying
    @Query("update User set lastLoginTime=?2 where login=?1")
    void updateLoginTime(String login, OffsetDateTime time);

    @Override
    @EntityGraph(attributePaths = {"roles", "books"})
    void deleteById(Long aLong);
}