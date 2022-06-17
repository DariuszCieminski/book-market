package pl.bookmarket.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.bookmarket.security.authentication.JwtService;
import pl.bookmarket.security.authentication.JwtServiceImpl;
import pl.bookmarket.testhelpers.datafactory.AuthenticationFactory;
import pl.bookmarket.util.AuthUtils;
import pl.bookmarket.validation.RestControllerExceptionHandler;

import javax.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {AuthController.class, JwtServiceImpl.class, RestControllerExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWebMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Value("${bm.controllers.auth}")
    private String authControllerUrl;

    @Test
    void shouldReturnValidRefreshToken() throws Exception {
        Authentication authentication = AuthenticationFactory.getAuthenticatedUser(1L);
        String accessToken = jwtService.generateAccessToken(authentication);
        String refreshToken = jwtService.generateRefreshToken(authentication);
        Cookie refreshCookie = AuthUtils.getRefreshTokenCookie(refreshToken);

        MvcResult result = mockMvc.perform(post(authControllerUrl + "/refresh-token")
                                          .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                          .cookie(refreshCookie))
                                  .andExpect(status().isOk())
                                  .andExpect(jsonPath("accessToken").exists())
                                  .andReturn();

        String newRefreshToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        assertNotNull(newRefreshToken);
        assertTrue(jwtService.validateToken(newRefreshToken));
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshingWithInvalidAccessToken() throws Exception {
        Authentication authentication = AuthenticationFactory.getAuthenticatedUser(1L);
        String accessToken = "INVALID_TOKEN";
        String refreshToken = jwtService.generateRefreshToken(authentication);
        Cookie refreshCookie = AuthUtils.getRefreshTokenCookie(refreshToken);

        mockMvc.perform(post(authControllerUrl + "/refresh-token")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                       .cookie(refreshCookie))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("accessToken").doesNotHaveJsonPath());
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshingWithInvalidRefreshToken() throws Exception {
        Authentication authentication = AuthenticationFactory.getAuthenticatedUser(1L);
        String accessToken = jwtService.generateAccessToken(authentication);
        String refreshToken = "INVALID_TOKEN";
        Cookie refreshCookie = AuthUtils.getRefreshTokenCookie(refreshToken);

        mockMvc.perform(post(authControllerUrl + "/refresh-token")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                       .cookie(refreshCookie))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("accessToken").doesNotHaveJsonPath());
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshingWithNoAccessToken() throws Exception {
        Authentication authentication = AuthenticationFactory.getAuthenticatedUser(1L);
        String refreshToken = jwtService.generateRefreshToken(authentication);
        Cookie refreshCookie = AuthUtils.getRefreshTokenCookie(refreshToken);

        mockMvc.perform(post(authControllerUrl + "/refresh-token")
                       .cookie(refreshCookie))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("accessToken").doesNotHaveJsonPath());
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshingWithNoRefreshToken() throws Exception {
        Authentication authentication = AuthenticationFactory.getAuthenticatedUser(1L);
        String accessToken = jwtService.generateAccessToken(authentication);

        mockMvc.perform(post(authControllerUrl + "/refresh-token")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("accessToken").doesNotHaveJsonPath());
    }
}