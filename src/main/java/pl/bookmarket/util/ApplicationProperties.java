package pl.bookmarket.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationProperties {

    @Value("${bm.controllers.user}")
    private String usersApiUrl;

    @Value("${bm.controllers.role}")
    private String rolesApiUrl;

    @Value("${bm.controllers.book}")
    private String booksApiUrl;

    @Value("${bm.controllers.genre}")
    private String genresApiUrl;

    @Value("${bm.controllers.offer}")
    private String offersApiUrl;

    @Value("${bm.controllers.message}")
    private String messagesApiUrl;

    @Value("${bm.controllers.auth}")
    private String authApiUrl;

    @Value("${bm.login-url}")
    private String loginUrl;

    @Value("${server.error.path:/error}")
    private String errorControllerUrl;

    @Value("${bm.cors-origins}")
    private List<String> corsOrigins;

    public String getUsersApiUrl() {
        return usersApiUrl;
    }

    public void setUsersApiUrl(String usersApiUrl) {
        this.usersApiUrl = usersApiUrl;
    }

    public String getRolesApiUrl() {
        return rolesApiUrl;
    }

    public void setRolesApiUrl(String rolesApiUrl) {
        this.rolesApiUrl = rolesApiUrl;
    }

    public String getBooksApiUrl() {
        return booksApiUrl;
    }

    public void setBooksApiUrl(String booksApiUrl) {
        this.booksApiUrl = booksApiUrl;
    }

    public String getGenresApiUrl() {
        return genresApiUrl;
    }

    public void setGenresApiUrl(String genresApiUrl) {
        this.genresApiUrl = genresApiUrl;
    }

    public String getOffersApiUrl() {
        return offersApiUrl;
    }

    public void setOffersApiUrl(String offersApiUrl) {
        this.offersApiUrl = offersApiUrl;
    }

    public String getMessagesApiUrl() {
        return messagesApiUrl;
    }

    public void setMessagesApiUrl(String messagesApiUrl) {
        this.messagesApiUrl = messagesApiUrl;
    }

    public String getAuthApiUrl() {
        return authApiUrl;
    }

    public void setAuthApiUrl(String authApiUrl) {
        this.authApiUrl = authApiUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getErrorControllerUrl() {
        return errorControllerUrl;
    }

    public void setErrorControllerUrl(String errorControllerUrl) {
        this.errorControllerUrl = errorControllerUrl;
    }

    public List<String> getCorsOrigins() {
        return corsOrigins;
    }

    public void setCorsOrigins(List<String> corsOrigins) {
        this.corsOrigins = corsOrigins;
    }
}