package pl.bookmarket.dto;

import pl.bookmarket.validation.constraints.NotContain;
import pl.bookmarket.validation.constraints.UniqueEmail;
import pl.bookmarket.validation.constraints.UniqueLogin;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

public class UserCreateDto {

    @UniqueLogin
    @NotBlank(message = "login.empty")
    @Size(min = 3, message = "login.size.min")
    @Size(max = 20, message = "login.size.max")
    @NotContain(message = "login.forbidden", values = {"admin", "superuser"})
    @Pattern(message = "login.invalid", regexp =
            "[^!@#$%^&*()=+\\-/\\[\\]{};:'`,.?|]+|[!@#$%^&*()=+\\-/\\[\\]{};:'`,.?|]+(\\w+|\\d+)\\S+")
    private String login;

    @UniqueEmail
    @Email(message = "email.invalid", regexp = "^[a-zA-Z0-9]{2,64}@[a-zA-Z0-9]{2,250}\\.[a-zA-Z0-9]{2,3}$")
    private String email;

    @Pattern(message = "password.not.match.regex", regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")
    private String password;

    private Set<RoleDto> roles;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDto> roles) {
        this.roles = roles;
    }
}