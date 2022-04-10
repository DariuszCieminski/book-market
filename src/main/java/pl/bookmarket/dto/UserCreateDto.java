package pl.bookmarket.dto;

import pl.bookmarket.validation.ValidationGroups;
import pl.bookmarket.validation.constraints.NotContain;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

public class UserCreateDto {

    @NotBlank(message = "field.empty")
    @Size(min = 3, message = "login.size.min")
    @Size(max = 20, message = "login.size.max")
    @NotContain(message = "login.forbidden", values = {"admin", "superuser"})
    @Pattern(message = "login.invalid", regexp =
            "[^!@#$%^&*()=+\\-/\\[\\]{};:'`,.?|]+|[!@#$%^&*()=+\\-/\\[\\]{};:'`,.?|]+(\\w+|\\d+)\\S+")
    private String login;

    @NotBlank(message = "field.empty")
    @Email(message = "email.invalid", regexp = "^[a-zA-Z0-9]{2,64}@[a-zA-Z0-9]{2,250}\\.[a-zA-Z0-9]{2,3}$")
    private String email;

    @Null(groups = ValidationGroups.OnRegister.class, message = "field.not.null")
    @Pattern(groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class}, message = "password.invalid",
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!?@#$%^&*+-])[A-Za-z0-9!?@#$%^&*+-]{8,25}$")
    private String password;

    @Null(groups = ValidationGroups.OnRegister.class, message = "field.not.null")
    @NotEmpty(groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class}, message = "list.empty")
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