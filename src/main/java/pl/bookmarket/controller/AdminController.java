package pl.bookmarket.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.dao.GenreDao;
import pl.bookmarket.dao.RoleDao;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.Genre;
import pl.bookmarket.model.Role;
import pl.bookmarket.model.User;
import pl.bookmarket.util.CustomPasswordEncoder;
import pl.bookmarket.validation.ValidationGroups;
import pl.bookmarket.validation.exceptions.CustomException;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.ValidationException;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final GenreDao genreDao;

    @Autowired
    public AdminController(UserDao userDao, RoleDao roleDao, GenreDao genreDao) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.genreDao = genreDao;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        userDao.findAll().forEach(userList::add);

        return userList;
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Long id) {
        return userDao.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class, id));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@Validated(ValidationGroups.CreateUser.class) @RequestBody User user, BindingResult result) {
        if (result.hasErrors()) {
            throw new ValidationException(result.getFieldErrors());
        }

        user.setPassword(CustomPasswordEncoder.hash(user.getPassword()));
        return userDao.save(user);
    }

    @PutMapping("/users/{id}")
    public User editUser(@Valid @RequestBody User user, BindingResult result, @PathVariable Long id) {
        if (!id.equals(user.getId())) {
            throw new CustomException("ID mismatch", HttpStatus.BAD_REQUEST);
        }

        if (result.hasErrors()) {
            throw new ValidationException(result.getFieldErrors());
        }

        Optional<User> dbUser = userDao.findById(id);

        if (!dbUser.isPresent()) {
            throw new EntityNotFoundException(User.class, id);
        }

        if (user.getPassword() == null) {
            user.setPassword(dbUser.get().getPassword());
        } else {
            user.setPassword(CustomPasswordEncoder.hash(user.getPassword()));
        }

        return userDao.save(user);
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        if (!userDao.existsById(id)) {
            throw new EntityNotFoundException(User.class, id);
        }

        userDao.deleteById(id);

        return "{}";
    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        List<Role> roleList = new ArrayList<>();
        roleDao.findAll().forEach(roleList::add);

        return roleList;
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public Role addRole(@Valid @RequestBody Role role, BindingResult result) {
        if (result.hasErrors()) {
            throw new ValidationException(result.getFieldErrors());
        }

        return roleDao.save(role);
    }

    @PutMapping("/roles/{id}")
    public Role editRole(@Valid @RequestBody Role role, BindingResult result, @PathVariable Long id) {
        if (!id.equals(role.getId())) {
            throw new CustomException("ID mismatch", HttpStatus.BAD_REQUEST);
        }

        if (!roleDao.existsById(id)) {
            throw new EntityNotFoundException(Role.class, id);
        }

        if (result.hasErrors()) {
            throw new ValidationException(result.getFieldErrors());
        }

        return roleDao.save(role);
    }

    @DeleteMapping("/roles/{id}")
    public String deleteRole(@PathVariable Long id) {
        if (!roleDao.existsById(id)) {
            throw new EntityNotFoundException(Role.class, id);
        }

        roleDao.deleteById(id);

        return "{}";
    }

    @GetMapping("/genres")
    public List<Genre> getAllGenres() {
        List<Genre> genreList = new ArrayList<>();
        genreDao.findAll().forEach(genreList::add);

        return genreList;
    }

    @PostMapping("/genres")
    @ResponseStatus(HttpStatus.CREATED)
    public Genre addGenre(@Valid @RequestBody Genre genre, BindingResult result) {
        if (result.hasErrors()) {
            throw new ValidationException(result.getFieldErrors());
        }

        return genreDao.save(genre);
    }

    @PutMapping("/genres/{id}")
    public Genre editGenre(@Valid @RequestBody Genre genre, BindingResult result, @PathVariable Long id) {
        if (!id.equals(genre.getId())) {
            throw new CustomException("ID mismatch", HttpStatus.BAD_REQUEST);
        }

        if (!genreDao.existsById(id)) {
            throw new EntityNotFoundException(Genre.class, id);
        }

        if (result.hasErrors()) {
            throw new ValidationException(result.getFieldErrors());
        }

        return genreDao.save(genre);
    }

    @DeleteMapping("/genres/{id}")
    public String deleteGenre(@PathVariable Long id) {
        if (!genreDao.existsById(id)) {
            throw new EntityNotFoundException(Genre.class, id);
        }

        genreDao.deleteById(id);

        return "{}";
    }
}