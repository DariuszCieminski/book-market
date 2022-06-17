package pl.bookmarket.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.Role;
import pl.bookmarket.model.User;
import pl.bookmarket.testhelpers.datafactory.UserBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.bookmarket.testhelpers.utils.EqualsId.equalsId;

@SpringBootTest
@AutoConfigureMockMvc
class LoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserDao userDao;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Value("${bm.login-url}")
    private String loginUrl;

    @Test
    void shouldSuccessfullyAuthenticateUser() throws Exception {
        User user = UserBuilder.getDefaultUser().build();
        Map<String, String> userCredentials = new HashMap<>();
        userCredentials.put("username", user.getEmail());
        userCredentials.put("password", user.getPassword());
        Mockito.when(userDao.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        mockMvc.perform(post(loginUrl).secure(true)
                                      .content(mapper.writeValueAsString(userCredentials))
                                      .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.userId", equalsId(user.getId())))
               .andExpect(jsonPath("$.roles", hasSize(user.getRoles().size())))
               .andExpect(jsonPath("$.roles", containsInAnyOrder(user.getRoles().stream().map(Role::getName).toArray(String[]::new))))
               .andExpect(jsonPath("$.accessToken", not(blankOrNullString())));
    }

    @Test
    void shouldThrowBadCredentialsException() throws Exception {
        User user = UserBuilder.getDefaultUser().build();
        Map<String, String> userCredentials = new HashMap<>();
        userCredentials.put("username", user.getEmail());
        userCredentials.put("password", user.getPassword());
        Mockito.when(userDao.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        mockMvc.perform(post(loginUrl).secure(true)
                                      .content(mapper.writeValueAsString(userCredentials))
                                      .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldThrowUserNotFoundException() throws Exception {
        User user = UserBuilder.getDefaultUser().build();
        Map<String, String> userCredentials = new HashMap<>();
        userCredentials.put("username", user.getEmail());
        userCredentials.put("password", user.getPassword());
        Mockito.when(userDao.findUserByEmail(user.getEmail())).thenReturn(Optional.empty());

        mockMvc.perform(post(loginUrl).secure(true)
                                      .content(mapper.writeValueAsString(userCredentials))
                                      .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldThrowHttpRequestMethodNotSupportedException() throws Exception {
        User user = UserBuilder.getDefaultUser().build();
        Map<String, String> userCredentials = new HashMap<>();
        userCredentials.put("username", user.getEmail());
        userCredentials.put("password", user.getPassword());
        Mockito.when(userDao.findUserByEmail(user.getEmail())).thenReturn(Optional.empty());

        mockMvc.perform(put(loginUrl).secure(true)
                                     .content(mapper.writeValueAsString(userCredentials))
                                     .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isMethodNotAllowed());
    }
}