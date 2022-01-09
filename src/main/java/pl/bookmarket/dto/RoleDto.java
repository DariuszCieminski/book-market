package pl.bookmarket.dto;

import javax.validation.constraints.NotBlank;

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