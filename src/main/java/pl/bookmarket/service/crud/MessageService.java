package pl.bookmarket.service.crud;

import pl.bookmarket.model.Message;

import java.util.List;

public interface MessageService {
    List<Message> getReceivedMessages(Long userId);

    List<Message> getUnreadMessages(Long userId);

    List<Message> getAllMessages(Long userId);

    Message createMessage(Message message);

    List<Message> createMultipleMessages(List<Message> messages);

    Message updateMessage(Message message);

    void setMessagesRead(List<Long> messageIds);

    void deleteMessage(Long id);
}