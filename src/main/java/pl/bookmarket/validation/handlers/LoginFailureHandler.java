package pl.bookmarket.validation.handlers;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final String redirectUrl;

    public LoginFailureHandler() {
        this.redirectUrl = "/login";
    }

    public LoginFailureHandler(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        FlashMap flashMap = new FlashMap();
        flashMap.put("loginError", exception.getMessage());

        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.sendRedirect(redirectUrl);
    }
}