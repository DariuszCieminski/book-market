package pl.bookmarket.mapper;

import org.junit.jupiter.api.Test;
import pl.bookmarket.dto.MessageCreateDto;
import pl.bookmarket.dto.MessageDto;
import pl.bookmarket.model.Message;
import pl.bookmarket.testhelpers.datafactory.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageMapperTest {

    private final MessageMapper mapper = new MessageMapperImpl(new UserMapperImpl(new RoleMapperImpl()));

    @Test
    void messageToMessageDto() {
        Message message = MessageBuilder.getDefaultMessage().build();

        MessageDto messageDto = mapper.messageToMessageDto(message);

        assertEquals(message.getId(), messageDto.getId());
        assertEquals(message.getText(), messageDto.getText());
        assertEquals(message.getSender().getId(), messageDto.getSender().getId());
        assertEquals(message.getSender().getLogin(), messageDto.getSender().getLogin());
        assertEquals(message.getReceiver().getId(), messageDto.getReceiver().getId());
        assertEquals(message.getReceiver().getLogin(), messageDto.getReceiver().getLogin());
        assertEquals(message.isRead(), messageDto.isRead());
        assertEquals(message.getSendTime(), messageDto.getSendTime());
    }

    @Test
    void messageCreateDtoToMessage() {
        MessageCreateDto messageCreateDto = MessageBuilder.getDefaultMessage().buildMessageCreateDto();

        Message message = mapper.messageCreateDtoToMessage(messageCreateDto);

        assertEquals(messageCreateDto.getText(), message.getText());
        assertEquals(messageCreateDto.getReceiver().getId(), message.getReceiver().getId());
        assertEquals(messageCreateDto.getReceiver().getLogin(), message.getReceiver().getLogin());
    }
}