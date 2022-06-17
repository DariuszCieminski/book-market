package pl.bookmarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import pl.bookmarket.dto.RoleDto;
import pl.bookmarket.testhelpers.datafactory.AuthenticationFactory;
import pl.bookmarket.testhelpers.utils.WithAuthenticatedUser;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.bookmarket.testhelpers.utils.EqualsId.equalsId;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql(value = "/insertRoles.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(value = "/deleteRoles.sql", executionPhase = AFTER_TEST_METHOD)
@WithAuthenticatedUser(roles = "ADMIN")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Value("${bm.controllers.role}")
    private String roleControllerUrl;

    @Test
    void shouldSuccessfullyGetAllRoles() throws Exception {
        mockMvc.perform(get(roleControllerUrl).secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(1L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry("name", "USER"))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(2L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry("name", "ADMIN"))));
    }

    @Test
    void shouldThrow403WhenGettingAllRolesWithoutPermission() throws Exception {
        Authentication user = AuthenticationFactory.getAuthenticatedUser(1L);
        mockMvc.perform(get(roleControllerUrl).secure(true).with(authentication(user)))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldSuccessfullyGetRoleById() throws Exception {
        mockMvc.perform(get(roleControllerUrl + "/1").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(1L)))
               .andExpect(jsonPath("$.name", equalTo("USER")));
    }

    @Test
    void shouldThrow403WhenGettingRoleByIdWithoutPermission() throws Exception {
        Authentication user = AuthenticationFactory.getAuthenticatedUser(1L);
        mockMvc.perform(get(roleControllerUrl + "/1").secure(true).with(authentication(user)))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldThrow404WhenGettingNonExistingRoleById() throws Exception {
        mockMvc.perform(get(roleControllerUrl + "/999").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    @Sql(value = "/deleteRoles.sql", executionPhase = BEFORE_TEST_METHOD)
    @SqlMergeMode(MERGE)
    void shouldSuccessfullyCreateNewRole() throws Exception {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("DEVELOPER");

        mockMvc.perform(post(roleControllerUrl).secure(true)
                                               .content(mapper.writeValueAsString(roleDto))
                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", equalsId(1L)))
               .andExpect(jsonPath("$.name", equalTo(roleDto.getName())));
    }

    @Test
    void shouldThrow422WhenCreatingRoleWithExistingName() throws Exception {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("USER");

        mockMvc.perform(post(roleControllerUrl).secure(true)
                                               .content(mapper.writeValueAsString(roleDto))
                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("name")))));
    }

    @Test
    void shouldThrow422WhenCreatingRoleWithEmptyName() throws Exception {
        RoleDto roleDto = new RoleDto();

        mockMvc.perform(post(roleControllerUrl).secure(true)
                                               .content(mapper.writeValueAsString(roleDto))
                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("name")))));
    }

    @Test
    void shouldThrow403WhenCreatingRoleWithoutPermission() throws Exception {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("DEVELOPER");
        Authentication user = AuthenticationFactory.getAuthenticatedUser(1L);

        mockMvc.perform(post(roleControllerUrl).secure(true).with(authentication(user))
                                               .content(mapper.writeValueAsString(roleDto))
                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldSuccessfullyUpdateRole() throws Exception {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("DEVELOPER");

        mockMvc.perform(put(roleControllerUrl + "/1").secure(true)
                                                     .content(mapper.writeValueAsString(roleDto))
                                                     .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(1L)))
               .andExpect(jsonPath("$.name", equalTo(roleDto.getName())));
    }

    @Test
    void shouldThrow422WhenUpdatingRoleWithExistingName() throws Exception {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("USER");

        mockMvc.perform(put(roleControllerUrl + "/2").secure(true)
                                                     .content(mapper.writeValueAsString(roleDto))
                                                     .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("name")))));
    }

    @Test
    void shouldThrow422WhenUpdatingRoleWithEmptyName() throws Exception {
        RoleDto roleDto = new RoleDto();

        mockMvc.perform(put(roleControllerUrl + "/1").secure(true)
                                                     .content(mapper.writeValueAsString(roleDto))
                                                     .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("name")))));
    }

    @Test
    void shouldThrow403WhenUpdatingRoleWithoutPermission() throws Exception {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("DEVELOPER");
        Authentication user = AuthenticationFactory.getAuthenticatedUser(1L);

        mockMvc.perform(put(roleControllerUrl + "/1").secure(true).with(authentication(user))
                                                     .content(mapper.writeValueAsString(roleDto))
                                                     .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldThrow404WhenUpdatingNonExistingRole() throws Exception {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("DEVELOPER");

        mockMvc.perform(put(roleControllerUrl + "/999").secure(true)
                                                       .content(mapper.writeValueAsString(roleDto))
                                                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldSuccessfullyDeleteRole() throws Exception {
        mockMvc.perform(delete(roleControllerUrl + "/1").secure(true))
               .andExpect(status().isNoContent());
        mockMvc.perform(get(roleControllerUrl + "/1").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldThrow403WhenDeletingRoleWithoutPermission() throws Exception {
        Authentication user = AuthenticationFactory.getAuthenticatedUser(1L);

        mockMvc.perform(delete(roleControllerUrl + "/1").secure(true).with(authentication(user)))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldThrow404WhenDeletingNonExistingRole() throws Exception {
        mockMvc.perform(delete(roleControllerUrl + "/999").secure(true))
               .andExpect(status().isNotFound());
    }
}