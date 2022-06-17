package pl.bookmarket.dto;

import pl.bookmarket.validation.constraint.ResetPassword;

@ResetPassword
public class ResetPasswordDto {

    private String login;
    private String email;

    public ResetPasswordDto() {
    }

    public ResetPasswordDto(String login, String email) {
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