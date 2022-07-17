package pl.bookmarket.mapper;

import org.junit.jupiter.api.Test;
import pl.bookmarket.dto.RoleDto;
import pl.bookmarket.model.Role;
import pl.bookmarket.testhelpers.datafactory.RoleFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleMapperTest {

    private final RoleMapper mapper = new RoleMapperImpl();

    @Test
    void roleToDto() {
        Role role = RoleFactory.getDefaultRole();

        RoleDto roleDto = mapper.roleToDto(role);

        assertEquals(role.getId(), roleDto.getId());
        assertEquals(role.getName(), roleDto.getName());
    }

    @Test
    void dtoToRole() {
        RoleDto roleDto = new RoleDto();
        roleDto.setId(456L);
        roleDto.setName("RoleDto456");

        Role role = mapper.dtoToRole(roleDto);

        assertEquals(roleDto.getId(), role.getId());
        assertEquals(roleDto.getName(), role.getName());
    }
}