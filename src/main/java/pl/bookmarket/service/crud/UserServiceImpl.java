package pl.bookmarket.service.crud;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.User;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.EntityValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> getUserByLogin(String login) {
        return userDao.findUserByLogin(login);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userDao.findUserByEmail(email);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        userDao.findAll().forEach(userList::add);

        return userList;
    }

    @Override
    public User createUser(User user) {
        validateLoginAndEmail(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userDao.save(user);
    }

    @Override
    public User updateUser(User user) {
        User byId = userDao.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException(User.class));

        validateLoginAndEmail(user);

        if (user.getPassword() == null) {
            user.setPassword(byId.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userDao.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userDao.existsById(id)) {
            throw new EntityNotFoundException(User.class);
        }
        userDao.deleteById(id);
    }

    private void validateLoginAndEmail(User user) {
        User byLogin = userDao.findUserByLogin(user.getLogin()).orElse(null);

        if (byLogin != null && !byLogin.getId().equals(user.getId())) {
            throw new EntityValidationException("login", "login.occupied");
        }

        User byEmail = userDao.findUserByEmail(user.getEmail()).orElse(null);

        if (byEmail != null && !byEmail.getId().equals(user.getId())) {
            throw new EntityValidationException("email", "email.occupied");
        }
    }
}