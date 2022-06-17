package pl.bookmarket.testhelpers.datafactory;

import pl.bookmarket.dto.MessageCreateDto;
import pl.bookmarket.dto.UserSimpleDto;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.User;

import java.time.OffsetDateTime;

public class MessageBuilder {

    private static long id = 100L;
    private final Message message = new Message();

    public Message build() {
        return message;
    }

    public MessageCreateDto buildMessageCreateDto() {
        MessageCreateDto dto = new MessageCreateDto();
        dto.setText(message.getText());
        dto.setReceiver(userToSimpleDto(message.getReceiver()));
        return dto;
    }

    public MessageBuilder withId(Long id) {
        message.setId(id);
        return this;
    }

    public MessageBuilder withText(String text) {
        message.setText(text);
        return this;
    }

    public MessageBuilder withSender(User sender) {
        message.setSender(sender);
        return this;
    }

    public MessageBuilder withReceiver(User receiver) {
        message.setReceiver(receiver);
        return this;
    }

    public MessageBuilder withRead(boolean isRead) {
        message.setRead(isRead);
        return this;
    }

    public static MessageBuilder getDefaultMessage() {
        MessageBuilder builder = new MessageBuilder();
        builder.message.setId(id);
        builder.message.setText("Sample message " + id);
        builder.message.setSender(UserBuilder.getDefaultUser().withId(1L).withLogin("Sender").build());
        builder.message.setReceiver(UserBuilder.getDefaultUser().withId(2L).withLogin("Receiver").build());
        builder.message.setRead(false);
        builder.message.setSendTime(OffsetDateTime.parse("2022-05-09T10:15:37+01:00"));
        id++;
        return builder;
    }

    private UserSimpleDto userToSimpleDto(User user) {
        if (user == null) return null;
        UserSimpleDto userDto = new UserSimpleDto();
        userDto.setId(user.getId());
        userDto.setLogin(user.getLogin());
        return userDto;
    }
}