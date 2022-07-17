package pl.bookmarket.mapper;

import org.junit.jupiter.api.Test;
import pl.bookmarket.dto.UserCreateDto;
import pl.bookmarket.dto.UserDto;
import pl.bookmarket.dto.UserSimpleDto;
import pl.bookmarket.model.User;
import pl.bookmarket.testhelpers.datafactory.UserBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMapperTest {

    private final UserMapper mapper = new UserMapperImpl(new RoleMapperImpl());

    @Test
    void userToUserDto() {
        User user = UserBuilder.getDefaultUser().build();

        UserDto userDto = mapper.userToUserDto(user);

        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getLogin(), userDto.getLogin());
        assertEquals(user.getEmail(), userDto.getEmail());
        assertEquals(user.getRegisterDate(), userDto.getRegisterDate());
        assertEquals(user.getLastLoginTime(), userDto.getLastLoginTime());
        assertEquals(user.isBlocked(), userDto.isBlocked());
        assertEquals(user.getRoles().size(), userDto.getRoles().size());
    }

    @Test
    void userToUserSimpleDto() {
        User user = UserBuilder.getDefaultUser().build();

        UserSimpleDto userSimpleDto = mapper.userToUserSimpleDto(user);

        assertEquals(user.getId(), userSimpleDto.getId());
        assertEquals(user.getLogin(), userSimpleDto.getLogin());
    }

    @Test
    void userCreateDtoToUser() {
        UserCreateDto userCreateDto = UserBuilder.getDefaultUser().buildUserCreateDto();

        User user = mapper.userCreateDtoToUser(userCreateDto);

        assertEquals(userCreateDto.getLogin(), user.getLogin());
        assertEquals(userCreateDto.getEmail(), user.getEmail());
        assertEquals(userCreateDto.getPassword(), user.getPassword());
        assertEquals(userCreateDto.getRoles().size(), user.getRoles().size());
    }

    @Test
    void userSimpleDtoToUser() {
        UserSimpleDto userSimpleDto = new UserSimpleDto();
        userSimpleDto.setId(99L);
        userSimpleDto.setLogin("TestUser99");

        User user = mapper.userSimpleDtoToUser(userSimpleDto);

        assertEquals(userSimpleDto.getId(), user.getId());
        assertEquals(userSimpleDto.getLogin(), user.getLogin());
    }
}