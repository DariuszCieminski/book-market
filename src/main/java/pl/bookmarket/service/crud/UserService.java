package pl.bookmarket.service.crud;

import pl.bookmarket.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> getUserByLogin(String login);

    Optional<User> getUserByEmail(String email);

    Optional<User> getUserById(Long id);

    List<User> getAllUsers();

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Long id);
}