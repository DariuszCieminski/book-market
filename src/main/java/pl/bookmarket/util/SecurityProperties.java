package pl.bookmarket.util;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class SecurityProperties {

    @Value("${bm.controllers.user}/**")
    private String usersApiUrl;

    @Value("${bm.controllers.role}/**")
    private String rolesApiUrl;

    @Value("${bm.controllers.genre}/**")
    private String genresApiUrl;

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

    public String getGenresApiUrl() {
        return genresApiUrl;
    }

    public void setGenresApiUrl(String genresApiUrl) {
        this.genresApiUrl = genresApiUrl;
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