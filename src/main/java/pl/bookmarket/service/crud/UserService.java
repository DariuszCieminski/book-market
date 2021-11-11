package pl.bookmarket.service.crud;

import pl.bookmarket.model.User;

import java.util.List;

public interface UserService {
    User getUserByLogin(String login);

    User getUserByEmail(String email);

    User getUserById(Long id);

    List<User> getAllUsers();

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Long id);
}