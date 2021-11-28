package pl.bookmarket.dto;

import pl.bookmarket.validation.constraints.UniqueRole;

import javax.validation.constraints.NotBlank;

@UniqueRole
public class RoleDto {
    private Long id;

    @NotBlank(message = "field.blank")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}