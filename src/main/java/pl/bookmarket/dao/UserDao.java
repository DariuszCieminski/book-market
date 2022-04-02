package pl.bookmarket.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.bookmarket.model.User;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface UserDao extends CrudRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findUserByLogin(String login);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findUserByEmail(String email);

    @Override
    @EntityGraph(attributePaths = "roles")
    Iterable<User> findAll();

    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(Long aLong);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User set lastLoginTime=?2 where id=?1")
    void updateLastLoginTime(Long id, OffsetDateTime time);
}