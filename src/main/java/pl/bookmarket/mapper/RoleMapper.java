package pl.bookmarket.mapper;

import org.mapstruct.Mapper;
import pl.bookmarket.dto.RoleDto;
import pl.bookmarket.model.Role;

@Mapper
public interface RoleMapper {

    RoleDto roleToDto(Role role);

    Role dtoToRole(RoleDto roleDto);
}