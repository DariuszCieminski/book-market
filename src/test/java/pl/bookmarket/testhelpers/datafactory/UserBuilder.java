package pl.bookmarket.testhelpers.datafactory;

import pl.bookmarket.dto.RoleDto;
import pl.bookmarket.dto.UserCreateDto;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.Offer;
import pl.bookmarket.model.Role;
import pl.bookmarket.model.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserBuilder {

    private static long id = 100L;
    private final User user = new User();

    public User build() {
        return user;
    }

    public UserCreateDto buildUserCreateDto() {
        UserCreateDto dto = new UserCreateDto();
        dto.setLogin(user.getLogin());
        dto.setPassword(user.getPassword().startsWith("$2a$10$") ? user.getPassword().substring(5, 20) : user.getPassword());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles().stream().map(role -> {
            RoleDto roleDto = new RoleDto();
            roleDto.setId(role.getId());
            roleDto.setName(role.getName());
            return roleDto;
        }).collect(Collectors.toSet()));
        return dto;
    }

    public UserBuilder withId(Long id) {
        user.setId(id);
        return this;
    }

    public UserBuilder withLogin(String login) {
        user.setLogin(login);
        return this;
    }

    public UserBuilder withEmail(String email) {
        user.setEmail(email);
        return this;
    }

    public UserBuilder withPassword(String password) {
        user.setPassword(password);
        return this;
    }

    public UserBuilder withRegisterDate(OffsetDateTime registerDate) {
        user.setRegisterDate(registerDate);
        return this;
    }

    public UserBuilder withLastLoginTime(OffsetDateTime lastLoginTime) {
        user.setLastLoginTime(lastLoginTime);
        return this;
    }

    public UserBuilder withBlocked(boolean blocked) {
        user.setBlocked(blocked);
        return this;
    }

    public UserBuilder withRoles(Set<Role> roles) {
        user.setRoles(roles);
        return this;
    }

    public UserBuilder withBooks(Set<Book> books) {
        user.setBooks(books);
        return this;
    }

    public UserBuilder withOffers(Set<Offer> offers) {
        user.setOffers(offers);
        return this;
    }

    public UserBuilder withSentMessages(Set<Message> sentMessages) {
        user.setSentMessages(sentMessages);
        return this;
    }

    public UserBuilder withReceivedMessages(Set<Message> receivedMessages) {
        user.setReceivedMessages(receivedMessages);
        return this;
    }

    public static UserBuilder getDefaultUser() {
        UserBuilder userBuilder = new UserBuilder();
        userBuilder.user.setId(id);
        userBuilder.user.setLogin("TestUser" + id);
        userBuilder.user.setEmail(String.format("test%s@test.com", id));
        userBuilder.user.setPassword("$2a$10$QoBYLaoVwcVzJwETUG38T.WEQTAdFfqWds7KbigV4DqJP3vYpZV5q");
        userBuilder.user.setRegisterDate(OffsetDateTime.of(2022, 3, 1, 12, 0, 0, 0, ZoneOffset.ofHours(1)));
        userBuilder.user.setLastLoginTime(OffsetDateTime.now().minusMinutes(3));
        userBuilder.user.setBlocked(false);
        userBuilder.user.setRoles(Collections.singleton(RoleFactory.getDefaultRole()));
        userBuilder.user.setBooks(Collections.emptySet());
        userBuilder.user.setOffers(Collections.emptySet());
        userBuilder.user.setSentMessages(Collections.emptySet());
        userBuilder.user.setReceivedMessages(Collections.emptySet());
        id++;
        return userBuilder;
    }

    public static UserBuilder getAdminUser() {
        UserBuilder defaultUser = getDefaultUser();
        List<Role> roles = Arrays.asList(RoleFactory.getDefaultRole(), RoleFactory.getAdminRole());
        return defaultUser.withRoles(Collections.unmodifiableSet(new HashSet<>(roles)));
    }
}