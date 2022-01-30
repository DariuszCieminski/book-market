package pl.bookmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.dto.RoleDto;
import pl.bookmarket.mapper.RoleMapper;
import pl.bookmarket.model.Role;
import pl.bookmarket.service.crud.RoleService;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${bm.controllers.role}")
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    public RoleController(RoleService roleService, RoleMapper roleMapper) {
        this.roleService = roleService;
        this.roleMapper = roleMapper;
    }

    @GetMapping
    public List<RoleDto> getRoles() {
        return roleService.getAllRoles().stream().map(roleMapper::roleToDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public RoleDto getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id).orElseThrow(() -> new EntityNotFoundException(Role.class));
        return roleMapper.roleToDto(role);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleDto createRole(@Valid @RequestBody RoleDto role) {
        Role created = roleService.createRole(roleMapper.dtoToRole(role));
        return roleMapper.roleToDto(created);
    }

    @PutMapping("/{id}")
    public RoleDto updateRole(@Valid @RequestBody RoleDto role, @PathVariable Long id) {
        Role toBeUpdated = roleMapper.dtoToRole(role);
        toBeUpdated.setId(id);
        return roleMapper.roleToDto(roleService.updateRole(toBeUpdated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
    }
}