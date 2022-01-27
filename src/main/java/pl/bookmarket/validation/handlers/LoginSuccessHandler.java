package pl.bookmarket.validation.handlers;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.service.authentication.AuthenticatedUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserDao userDao;

    public LoginSuccessHandler(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!authentication.getName().equals("admin")) {
            userDao.updateLastLoginTime(((AuthenticatedUser) (authentication)).getId(), OffsetDateTime.now());
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}