package pl.bookmarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.thymeleaf.util.StringUtils;
import pl.bookmarket.dto.RoleDto;
import pl.bookmarket.dto.UserCreateDto;
import pl.bookmarket.testhelpers.datafactory.AuthenticationFactory;
import pl.bookmarket.testhelpers.datafactory.UserBuilder;
import pl.bookmarket.testhelpers.utils.WithAuthenticatedUser;
import pl.bookmarket.util.ApplicationProperties;

import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.bookmarket.testhelpers.utils.EqualsId.equalsId;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql(value = {"/insertRoles.sql", "/insertUsers.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(value = {"/deleteUsers.sql", "/deleteRoles.sql"}, executionPhase = AFTER_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ApplicationProperties properties;

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldReturnAllUsers() throws Exception {
        mockMvc.perform(get(properties.getUsersApiUrl()).secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(1L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(2L)))));
    }

    @Test
    @WithAuthenticatedUser
    void shouldReturn403WhenGettingAllUsersWithoutPermission() throws Exception {
        mockMvc.perform(get(properties.getUsersApiUrl()).secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithAuthenticatedUser(id = 2)
    void shouldReturnUserById() throws Exception {
        mockMvc.perform(get(properties.getUsersApiUrl() + "/2").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)))
               .andExpect(jsonPath("$.login", is("TestUser2")))
               .andExpect(jsonPath("$.email", is("testuser2@bookmarket.com")))
               .andExpect(jsonPath("$.registerDate", not(blankOrNullString())))
               .andExpect(jsonPath("$.lastLoginTime", not(blankOrNullString())))
               .andExpect(jsonPath("$.blocked", is(false)))
               .andExpect(jsonPath("$.roles", hasItem(hasEntry("name", "USER"))))
               .andExpect(jsonPath("$.roles", hasSize(1)));
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldReturnUserByIdAsAdmin() throws Exception {
        mockMvc.perform(get(properties.getUsersApiUrl() + "/2").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)));
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrowEntityNotFoundExceptionWhenGettingUserByInvalidId() throws Exception {
        mockMvc.perform(get(String.format("%s/999", properties.getUsersApiUrl())).secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithAuthenticatedUser(id = 2)
    void shouldReturn403WhenGettingAnotherUserByIdWithoutPermission() throws Exception {
        mockMvc.perform(get(properties.getUsersApiUrl() + "/1").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldSuccessfullyCreateUser() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getAdminUser().withLogin("CreatedUser").buildUserCreateDto();

        mockMvc.perform(post(properties.getUsersApiUrl()).secure(true).content(mapper.writeValueAsString(userCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", not(equalsId(null))))
               .andExpect(jsonPath("$.login", is(userCreateDto.getLogin())))
               .andExpect(jsonPath("$.email", is(userCreateDto.getEmail())))
               .andExpect(jsonPath("$.blocked", is(false)))
               .andExpect(jsonPath("$.registerDate", notNullValue()))
               .andExpect(jsonPath("$.lastLoginTime", nullValue()))
               .andExpect(jsonPath("$.roles", hasSize(userCreateDto.getRoles().size())));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("getInvalidLogins")
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrowStatus422WhenCreatingUserWithInvalidLogin(String login) throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getAdminUser().withLogin(login).buildUserCreateDto();

        mockMvc.perform(post(properties.getUsersApiUrl()).secure(true).content(mapper.writeValueAsString(userCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "login"))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("getInvalidEmails")
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrowStatus422WhenCreatingUserWithInvalidEmail(String email) throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getAdminUser().withEmail(email).buildUserCreateDto();

        mockMvc.perform(post(properties.getUsersApiUrl()).secure(true).content(mapper.writeValueAsString(userCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "email"))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("getInvalidPasswords")
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrowStatus422WhenCreatingUserWithInvalidPassword(String password) throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getAdminUser().buildUserCreateDto();
        userCreateDto.setPassword(password);

        mockMvc.perform(post(properties.getUsersApiUrl()).secure(true).content(mapper.writeValueAsString(userCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "password"))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrowStatus422WhenCreatingUserWithInvalidRoles(Set<RoleDto> roles) throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getAdminUser().buildUserCreateDto();
        userCreateDto.setRoles(roles);

        mockMvc.perform(post(properties.getUsersApiUrl()).secure(true).content(mapper.writeValueAsString(userCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "roles"))));
    }

    @Test
    @WithAuthenticatedUser
    void shouldReturn403WhenCreatingUserWithoutPermission() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getDefaultUser().buildUserCreateDto();

        mockMvc.perform(post(properties.getUsersApiUrl()).secure(true).content(mapper.writeValueAsString(userCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldRegisterNewUserSuccessfully() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getDefaultUser().buildUserCreateDto();
        userCreateDto.setPassword(null);
        userCreateDto.setRoles(null);

        mockMvc.perform(post(properties.getUsersApiUrl() + "/register").secure(true)
                                                                       .content(mapper.writeValueAsString(userCreateDto))
                                                                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", not(equalsId(null))))
               .andExpect(jsonPath("$.login", is(userCreateDto.getLogin())))
               .andExpect(jsonPath("$.email", is(userCreateDto.getEmail())))
               .andExpect(jsonPath("$.blocked", is(false)))
               .andExpect(jsonPath("$.registerDate", notNullValue()))
               .andExpect(jsonPath("$.lastLoginTime", nullValue()))
               .andExpect(jsonPath("$.roles", hasSize(1)))
               .andExpect(jsonPath("$.roles", hasItem(hasValue("USER"))));
    }

    @Test
    void shouldThrowStatus422WhenRegisteringUserWithNotNullPassword() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getDefaultUser().buildUserCreateDto();
        userCreateDto.setRoles(null);

        mockMvc.perform(post(properties.getUsersApiUrl() + "/register").secure(true)
                                                                       .content(mapper.writeValueAsString(userCreateDto))
                                                                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "password"))));
    }

    @Test
    void shouldThrowStatus422WhenRegisteringUserWithNotNullRoles() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getAdminUser().buildUserCreateDto();
        userCreateDto.setPassword(null);

        mockMvc.perform(post(properties.getUsersApiUrl() + "/register").secure(true)
                                                                       .content(mapper.writeValueAsString(userCreateDto))
                                                                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "roles"))));
    }

    @Test
    @WithAuthenticatedUser(id = 2)
    void shouldUpdateNewUserSuccessfully() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getDefaultUser().buildUserCreateDto();
        userCreateDto.setLogin("UpdatedLogin2");

        mockMvc.perform(put(properties.getUsersApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(userCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)))
               .andExpect(jsonPath("$.login", is(userCreateDto.getLogin())))
               .andExpect(jsonPath("$.email", is(userCreateDto.getEmail())))
               .andExpect(jsonPath("$.roles", hasSize(1)))
               .andExpect(jsonPath("$.roles", hasItem(hasValue("USER"))));
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldUpdateNewUserSuccessfullyAsAdmin() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getDefaultUser().buildUserCreateDto();
        userCreateDto.setLogin("UpdatedLogin2");

        mockMvc.perform(put(properties.getUsersApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(userCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)));
    }

    @Test
    @WithAuthenticatedUser(id = 2)
    void shouldThrowStatus422WhenUpdatingUserWithNullPassword() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getDefaultUser().buildUserCreateDto();
        userCreateDto.setPassword(null);

        mockMvc.perform(put(properties.getUsersApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(userCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "password"))));
    }

    @Test
    @WithAuthenticatedUser(id = 2)
    void shouldThrowStatus422WhenUpdatingUserWithNullRoles() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getAdminUser().buildUserCreateDto();
        userCreateDto.setRoles(null);

        mockMvc.perform(put(properties.getUsersApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(userCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "roles"))));
    }

    @Test
    @WithAuthenticatedUser(id = 2)
    void shouldThrowStatus403WhenUpdatingOtherUserWithoutPermissions() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getAdminUser().buildUserCreateDto();
        userCreateDto.setEmail("newemail123@test.com");

        mockMvc.perform(put(properties.getUsersApiUrl() + "/1").secure(true)
                                                               .content(mapper.writeValueAsString(userCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithAuthenticatedUser(id = 1, roles = "ADMIN")
    void shouldThrowStatus404WhenUpdatingNonExistentUser() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getAdminUser().buildUserCreateDto();
        userCreateDto.setEmail("newemail123@test.com");

        mockMvc.perform(put(properties.getUsersApiUrl() + "/999").secure(true)
                                                                 .content(mapper.writeValueAsString(userCreateDto))
                                                                 .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithAuthenticatedUser(id = 2)
    void shouldSuccessfullyPatchUser() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getDefaultUser().withLogin("PatchedLogin2")
                                                 .withPassword("N3wP@s$w0rd123")
                                                 .withEmail("newemail123@test.com").buildUserCreateDto();

        mockMvc.perform(patch(properties.getUsersApiUrl() + "/2").secure(true)
                                                                 .content(mapper.writeValueAsString(userCreateDto))
                                                                 .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @EmptySource
    @MethodSource("getInvalidPasswords")
    @WithAuthenticatedUser(id = 2)
    void shouldThrowStatus422WhenPatchingUserWithInvalidPassword(String password) throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getDefaultUser().buildUserCreateDto();
        userCreateDto.setPassword(password);

        mockMvc.perform(patch(properties.getUsersApiUrl() + "/2").secure(true)
                                                                 .content(mapper.writeValueAsString(userCreateDto))
                                                                 .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity());
    }

    @ParameterizedTest
    @EmptySource
    @MethodSource("getInvalidLogins")
    @WithAuthenticatedUser(id = 2)
    void shouldThrowStatus422WhenPatchingUserWithInvalidLogin(String login) throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getDefaultUser().buildUserCreateDto();
        userCreateDto.setLogin(login);

        mockMvc.perform(patch(properties.getUsersApiUrl() + "/2").secure(true)
                                                                 .content(mapper.writeValueAsString(userCreateDto))
                                                                 .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity());
    }

    @ParameterizedTest
    @EmptySource
    @MethodSource("getInvalidEmails")
    @WithAuthenticatedUser(id = 2)
    void shouldThrowStatus422WhenPatchingUserWithInvalidEmail(String email) throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getDefaultUser().buildUserCreateDto();
        userCreateDto.setEmail(email);

        mockMvc.perform(patch(properties.getUsersApiUrl() + "/2").secure(true)
                                                                 .content(mapper.writeValueAsString(userCreateDto))
                                                                 .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithAuthenticatedUser(id = 2)
    void shouldThrowStatus403WhenPatchingOtherUserWithoutPermissions() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getAdminUser().buildUserCreateDto();
        userCreateDto.setEmail("newemail123@test.com");

        mockMvc.perform(patch(properties.getUsersApiUrl() + "/1").secure(true)
                                                                 .content(mapper.writeValueAsString(userCreateDto))
                                                                 .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithAuthenticatedUser(id = 1, roles = "ADMIN")
    void shouldThrowStatus404WhenPatchingNonExistentUser() throws Exception {
        UserCreateDto userCreateDto = UserBuilder.getAdminUser().buildUserCreateDto();
        userCreateDto.setEmail("newemail123@test.com");

        mockMvc.perform(patch(properties.getUsersApiUrl() + "/999").secure(true)
                                                                   .content(mapper.writeValueAsString(userCreateDto))
                                                                   .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldSuccessfullyDeleteUser() throws Exception {
        mockMvc.perform(delete(properties.getUsersApiUrl() + "/2").secure(true))
               .andExpect(status().isNoContent());
        mockMvc.perform(get(properties.getUsersApiUrl() + "/2").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    @Sql(value = "/insertAllData.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(value = "/deleteAllData.sql", executionPhase = AFTER_TEST_METHOD)
    void shouldSuccessfullyRemoveAllBooksMessagesOffersAfterDeletingUser() throws Exception {
        Authentication userToDelete = AuthenticationFactory.getAuthenticatedUser(2L);
        mockMvc.perform(get(properties.getUsersApiUrl() + "/2/books").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get(properties.getUsersApiUrl() + "/2/offers").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get(properties.getUsersApiUrl() + "/2/messages").secure(true)
                                                                        .with(authentication(userToDelete)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(delete(properties.getUsersApiUrl() + "/2").secure(true))
               .andExpect(status().isNoContent());

        mockMvc.perform(get(properties.getUsersApiUrl() + "/2/books").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", empty()));
        mockMvc.perform(get(properties.getUsersApiUrl() + "/2/offers").secure(true))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.errors[0].field", equalTo("user.id")))
               .andExpect(jsonPath("$.errors[0].errorCode", equalTo("not.found")));
        mockMvc.perform(get(properties.getUsersApiUrl() + "/2/messages").secure(true)
                                                                        .with(authentication(userToDelete)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", empty()));
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrow404WhenDeletingNonExistentUser() throws Exception {
        mockMvc.perform(delete(properties.getUsersApiUrl() + "/999").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithAuthenticatedUser(id = 2)
    void shouldThrow403WhenDeletingUserWithoutPermissions() throws Exception {
        mockMvc.perform(delete(properties.getUsersApiUrl() + "/1").secure(true))
               .andExpect(status().isForbidden());
    }

    private static Stream<String> getInvalidLogins() {
        return Stream.of("ab", StringUtils.repeat("a", 21), "Administrator", "SUPERUSER", "!@#$%^&*()-+=',.?", "TestUser!");
    }

    private static Stream<String> getInvalidEmails() {
        return Stream.of("email", "test@email", "test@email.x", "@email.com", "test@.com", "@", "a@b.pl", "test@email.book", String.format("%s@email.com", StringUtils.repeat("x", 65)), String.format("test@%s.com", StringUtils.repeat("x", 251)));
    }

    private static Stream<String> getInvalidPasswords() {
        return Stream.of("P$w0rd", String.format("P@ssw0r%s", StringUtils.repeat("D", 19)), "password", "PASSWORD", "!@#$%^&*()", "1234567890", "Pas$word", "Passw0rd", "pas$w0rd", "Passw0rd");
    }
}