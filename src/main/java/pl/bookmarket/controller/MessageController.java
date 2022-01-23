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
import pl.bookmarket.model.Message;
import pl.bookmarket.service.crud.MessageService;

import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.util.List;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("${bm.controllers.user}/{id}/messages")
    public List<Message> getMessages(@RequestParam(defaultValue = "all") MessageFilter filter, @PathVariable Long id) {
        if (filter == MessageFilter.UNREAD) {
            return messageService.getUnreadMessages(id);
        } else if (filter == MessageFilter.RECEIVED) {
            return messageService.getReceivedMessages(id);
        } else {
            return messageService.getAllMessages(id);
        }
    }

    @PostMapping("${bm.controllers.message}")
    @ResponseStatus(HttpStatus.CREATED)
    public Message sendMessage(@Valid @RequestBody Message message) {
        return messageService.createMessage(message);
    }

    @PutMapping("${bm.controllers.message}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setMessagesReadForUser(@RequestParam("ids") List<Long> messageIdsList) {
        messageService.setMessagesRead(messageIdsList);
    }

    @PutMapping("${bm.controllers.message}/{id}")
    public Message updateMessage(@Valid @RequestBody Message message, @PathVariable Long id) {
        return messageService.updateMessage(message);
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