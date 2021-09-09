package pl.bookmarket.service.crud;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.User;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getUserByLogin(String login) {
        return userDao.findUserByLogin(login).orElseThrow(() -> new EntityNotFoundException(User.class));
    }

    @Override
    public User getUserByEmail(String email) {
        return userDao.findUserByEmail(email).orElseThrow(() -> new EntityNotFoundException(User.class));
    }

    @Override
    public User getUserById(Long id) {
        return userDao.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class));
    }

    @Override
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        userDao.findAll().forEach(userList::add);

        return userList;
    }

    @Override
    public List<String> getUsersLogins() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return userDao.getUserLogins(currentUser);
    }

    @Override
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userDao.save(user);
    }

    @Override
    public User updateUser(User user) {
        User u = userDao.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException(User.class));

        if (user.getPassword() == null) {
            user.setPassword(u.getPassword());
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
}