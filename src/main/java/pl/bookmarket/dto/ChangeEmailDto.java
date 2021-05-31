package pl.bookmarket.dto;

import pl.bookmarket.validation.constraints.ChangeEmail;

@ChangeEmail
public class ChangeEmailDto {

    private String password;
    private String newEmail;
    private String confirmNewEmail;

    public ChangeEmailDto() {
    }

    public ChangeEmailDto(String password, String newEmail, String confirmNewEmail) {
        this.password = password;
        this.newEmail = newEmail;
        this.confirmNewEmail = confirmNewEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getConfirmNewEmail() {
        return confirmNewEmail;
    }

    public void setConfirmNewEmail(String confirmNewEmail) {
        this.confirmNewEmail = confirmNewEmail;
    }
}