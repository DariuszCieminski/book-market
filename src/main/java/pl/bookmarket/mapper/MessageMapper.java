package pl.bookmarket.mapper;

import org.mapstruct.Mapper;
import pl.bookmarket.dto.MessageCreateDto;
import pl.bookmarket.dto.MessageDto;
import pl.bookmarket.model.Message;

@Mapper(uses = UserMapper.class)
public interface MessageMapper {

    MessageDto messageToMessageDto(Message message);

    Message messageDtoToMessage(MessageDto messageDto);

    Message messageCreateDtoToMessage(MessageCreateDto messageCreateDto);
}