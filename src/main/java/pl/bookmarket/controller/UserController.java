package pl.bookmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.dto.UserCreateDto;
import pl.bookmarket.dto.UserDto;
import pl.bookmarket.mapper.UserMapper;
import pl.bookmarket.model.User;
import pl.bookmarket.service.crud.UserService;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${bm.controllers.user}")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getAllUsers().stream().map(userMapper::userToUserDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id).orElseThrow(() -> new EntityNotFoundException(User.class));
        return userMapper.userToUserDto(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserCreateDto user) {
        User created = userService.createUser(userMapper.userCreateDtoToUser(user));
        return userMapper.userToUserDto(created);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@Valid @RequestBody UserCreateDto user, @PathVariable Long id) {
        User toBeUpdated = userMapper.userCreateDtoToUser(user);
        toBeUpdated.setId(id);
        return userMapper.userToUserDto(userService.updateUser(toBeUpdated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}