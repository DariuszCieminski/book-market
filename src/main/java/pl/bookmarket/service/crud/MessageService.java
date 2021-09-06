package pl.bookmarket.service.crud;

import pl.bookmarket.model.Message;

import java.util.List;

public interface MessageService {
    List<Message> getReceivedMessages(String login);

    List<Message> getUnreadMessages(String login);

    List<Message> getAllMessages(String login);

    Message createMessage(Message message);

    List<Message> createMultipleMessages(List<Message> messages);

    Message updateMessage(Message message);

    void setMessagesRead(List<Long> messageIds);

    void deleteMessage(Long id);
}