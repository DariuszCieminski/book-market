package pl.bookmarket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonView;
import java.time.OffsetDateTime;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import pl.bookmarket.util.Views;
import pl.bookmarket.validation.ValidationGroups;
import pl.bookmarket.validation.constraints.NotContain;
import pl.bookmarket.validation.constraints.UniqueLoginAndEmail;

@Entity
@Table(name = "customer")
@UniqueLoginAndEmail(groups = ValidationGroups.CreateUser.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userGenerator")
    @SequenceGenerator(name = "userGenerator", sequenceName = "user_sequence", initialValue = 100, allocationSize = 1)
    private Long id;

    @NotBlank(message = "{login.not.blank}")
    @Size(min = 2, message = "{login.size.min}")
    @Size(max = 20, message = "{login.size.max}")
    @NotContain(message = "{login.forbidden}", values = {"admin", "superuser"})
    @Pattern(message = "{login.invalid}", regexp =
        "[^!@#$%^&*()=+\\-/\\[\\]{};:'`,.?|]+|[!@#$%^&*()=+\\-/\\[\\]{};:'`,.?|]+(\\w+|\\d+)\\S+")
    @JsonView({Views.Market.class, Views.Message.class, Views.Offer.class})
    private String login;

    @Email(message = "{email.invalid}", regexp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\""
                                                 + "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f"
                                                 + "]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9]"
                                                 + "(?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:"
                                                 + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
                                                 + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:"
                                                 + "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01"
                                                 + "-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
    private String email;

    @JsonProperty(access = Access.WRITE_ONLY)
    @Pattern(message = "{password.not.match.regex}", regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")
    private String password;

    @Column(name = "registered_on", updatable = false)
    @CreationTimestamp
    private OffsetDateTime registerDate;

    @Column(name = "last_login")
    private OffsetDateTime lastLoginTime;

    @Column(name = "is_blocked")
    private boolean blocked;

    @ManyToMany
    @JoinTable(joinColumns = {@JoinColumn(name = "user_id")}, inverseJoinColumns = {@JoinColumn(name = "role_id")})
    @JsonIgnoreProperties("users")
    private Set<Role> roles;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = "owner", allowSetters = true)
    private Set<Book> books;

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<Offer> offers;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<Message> sentMessages;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<Message> receivedMessages;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public OffsetDateTime getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(OffsetDateTime registerDate) {
        this.registerDate = registerDate;
    }

    public OffsetDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(OffsetDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }

    public Set<Offer> getOffers() {
        return offers;
    }

    public void setOffers(Set<Offer> offers) {
        this.offers = offers;
    }

    public Set<Message> getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(Set<Message> sentMessages) {
        this.sentMessages = sentMessages;
    }

    public Set<Message> getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(Set<Message> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }
}