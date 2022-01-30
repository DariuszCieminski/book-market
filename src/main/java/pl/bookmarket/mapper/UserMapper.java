package pl.bookmarket.mapper;

import org.mapstruct.Mapper;
import pl.bookmarket.dto.UserCreateDto;
import pl.bookmarket.dto.UserDto;
import pl.bookmarket.dto.UserSimpleDto;
import pl.bookmarket.model.User;

@Mapper(uses = RoleMapper.class)
public interface UserMapper {

    UserDto userToUserDto(User user);

    UserSimpleDto userToUserSimpleDto(User user);

    User userCreateDtoToUser(UserCreateDto userCreateDto);

    User userSimpleDtoToUser(UserSimpleDto userSimpleDto);
}