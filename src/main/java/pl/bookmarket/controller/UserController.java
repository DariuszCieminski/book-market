package pl.bookmarket.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import pl.bookmarket.validation.ValidationGroups;
import pl.bookmarket.validation.exception.EntityNotFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static pl.bookmarket.util.ObjectUtils.getFieldNamesByValueCondition;

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
    @PreAuthorize("authentication.principal.id == #id or hasRole('ADMIN')")
    public UserDto getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id).orElseThrow(() -> new EntityNotFoundException(User.class));
        return userMapper.userToUserDto(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto createUser(@Validated(ValidationGroups.OnCreate.class) @RequestBody UserCreateDto user) {
        User created = userService.createUser(userMapper.userCreateDtoToUser(user));
        return userMapper.userToUserDto(created);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto registerUser(@Validated(ValidationGroups.OnRegister.class) @RequestBody UserCreateDto user) {
        User registered = userService.createUser(userMapper.userCreateDtoToUser(user));
        return userMapper.userToUserDto(registered);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@Validated(ValidationGroups.OnUpdate.class) @RequestBody UserCreateDto user, @PathVariable Long id) {
        User toBeUpdated = userMapper.userCreateDtoToUser(user);
        toBeUpdated.setId(id);
        return userMapper.userToUserDto(userService.updateUser(toBeUpdated));
    }

    @PatchMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchUser(@RequestBody UserCreateDto user, @PathVariable Long id) {
        user.setRoles(null);
        Set<ConstraintViolation<UserCreateDto>> constraintViolations = validateObjectFields(user, getFieldNamesByValueCondition(user, Objects::nonNull));
        if (constraintViolations.isEmpty()) {
            User toBeUpdated = new User(userService.getUserById(id).orElseThrow(() -> new EntityNotFoundException(User.class)));
            BeanUtils.copyProperties(user, toBeUpdated, getFieldNamesByValueCondition(user, Objects::isNull));
            userService.updateUser(toBeUpdated);
        } else {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    private Set<ConstraintViolation<UserCreateDto>> validateObjectFields(UserCreateDto user, String... fieldsToValidate) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<UserCreateDto>> violations = new HashSet<>(fieldsToValidate.length);
        for (String field : fieldsToValidate) {
            violations.addAll(validator.validateProperty(user, field, ValidationGroups.OnUpdate.class));
        }
        return violations;
    }
}