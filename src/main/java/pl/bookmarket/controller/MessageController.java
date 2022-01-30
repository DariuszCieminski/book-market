package pl.bookmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.dto.MessageCreateDto;
import pl.bookmarket.dto.MessageDto;
import pl.bookmarket.mapper.MessageMapper;
import pl.bookmarket.model.Message;
import pl.bookmarket.service.crud.MessageService;

import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MessageController {

    private final MessageService messageService;
    private final MessageMapper messageMapper;

    public MessageController(MessageService messageService, MessageMapper messageMapper) {
        this.messageService = messageService;
        this.messageMapper = messageMapper;
    }

    @GetMapping("${bm.controllers.user}/{id}/messages")
    public List<MessageDto> getMessages(@RequestParam(defaultValue = "all") MessageFilter filter, @PathVariable Long id) {
        List<Message> messageList;
        if (filter == MessageFilter.UNREAD) {
            messageList = messageService.getUnreadMessages(id);
        } else if (filter == MessageFilter.RECEIVED) {
            messageList = messageService.getReceivedMessages(id);
        } else {
            messageList = messageService.getAllMessages(id);
        }
        return messageList.stream().map(messageMapper::messageToMessageDto).collect(Collectors.toList());
    }

    @PostMapping("${bm.controllers.message}")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto sendMessage(@Valid @RequestBody MessageCreateDto message) {
        Message created = messageService.createMessage(messageMapper.messageCreateDtoToMessage(message));
        return messageMapper.messageToMessageDto(created);
    }

    @PutMapping("${bm.controllers.message}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setMessagesReadForUser(@RequestParam("ids") List<Long> messageIdsList) {
        messageService.setMessagesRead(messageIdsList);
    }

    @PutMapping("${bm.controllers.message}/{id}")
    public MessageDto updateMessage(@Valid @RequestBody MessageDto message, @PathVariable Long id) {
        Message toBeUpdated = messageMapper.messageDtoToMessage(message);
        toBeUpdated.setId(id);
        return messageMapper.messageToMessageDto(messageService.updateMessage(toBeUpdated));
    }

    @DeleteMapping("${bm.controllers.message}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(MessageFilter.class, new MessageFilterConverter());
    }

    private enum MessageFilter {
        ALL,
        RECEIVED,
        UNREAD
    }

    private static class MessageFilterConverter extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            for (MessageFilter filterValue : MessageFilter.values()) {
                if (filterValue.toString().equalsIgnoreCase(text)) {
                    setValue(filterValue);
                }
            }
        }
    }
}