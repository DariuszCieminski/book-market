package pl.bookmarket.model;

import com.fasterxml.jackson.annotation.JsonView;
import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.lang.Nullable;
import pl.bookmarket.util.Views;
import pl.bookmarket.validation.ValidationGroups;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messageGenerator")
    @SequenceGenerator(name = "messageGenerator", sequenceName = "message_sequence", allocationSize = 1)
    @JsonView(Views.Message.class)
    private Long id;

    @Size(max = 300, message = "{message.too.long}")
    @JsonView(Views.Message.class)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonView(Views.Message.class)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "{receiver.not.set}", groups = ValidationGroups.SendMessage.class)
    @JoinColumn(name = "receiver_id", nullable = false)
    @JsonView(Views.Message.class)
    private User receiver;

    @Column(name = "is_read")
    @JsonView(Views.Message.class)
    private boolean read;

    @Column(name = "sent_on", nullable = false, updatable = false)
    @CreationTimestamp
    @JsonView(Views.Message.class)
    private OffsetDateTime sendTime;

    public Message() {
    }

    public Message(@Nullable User sender, User receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = message;
    }

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

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
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