package pl.bookmarket.dto;

import java.time.OffsetDateTime;

public class MessageDto {
    private Long id;
    private String text;
    private UserSimpleDto sender;
    private UserSimpleDto receiver;
    private boolean read;
    private OffsetDateTime sendTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UserSimpleDto getSender() {
        return sender;
    }

    public void setSender(UserSimpleDto sender) {
        this.sender = sender;
    }

    public UserSimpleDto getReceiver() {
        return receiver;
    }

    public void setReceiver(UserSimpleDto receiver) {
        this.receiver = receiver;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public OffsetDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(OffsetDateTime sendTime) {
        this.sendTime = sendTime;
    }
}