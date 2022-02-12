package pl.bookmarket.security.handlers;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.security.authentication.AuthenticatedUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;

public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserDao userDao;

    public LoginSuccessHandler(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Long userId = ((AuthenticatedUser) authentication.getPrincipal()).getId();
        userDao.updateLastLoginTime(userId, OffsetDateTime.now());
        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
    }
}