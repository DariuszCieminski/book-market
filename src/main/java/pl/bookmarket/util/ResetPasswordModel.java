package pl.bookmarket.util;

import pl.bookmarket.validation.constraints.ResetPassword;

@ResetPassword
public class ResetPasswordModel {

    private String login;
    private String email;

    public ResetPasswordModel() {
    }

    public ResetPasswordModel(String login, String email) {
        this.login = login;
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}