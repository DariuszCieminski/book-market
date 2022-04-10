package pl.bookmarket.model;

import org.hibernate.annotations.CreationTimestamp;

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
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "customer")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userGenerator")
    @SequenceGenerator(name = "userGenerator", sequenceName = "user_sequence", initialValue = 100, allocationSize = 1)
    private Long id;

    private String login;

    private String email;

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
    private Set<Role> roles;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE)
    private Set<Book> books;

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.REMOVE)
    private Set<Offer> offers;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.REMOVE)
    private Set<Message> sentMessages;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.REMOVE)
    private Set<Message> receivedMessages;

    public User() {
    }

    public User(User that) {
        this.id = that.id;
        this.login = that.login;
        this.email = that.email;
        this.password = that.password;
        this.registerDate = that.registerDate;
        this.lastLoginTime = that.lastLoginTime;
        this.blocked = that.blocked;
        this.roles = that.roles;
        this.books = that.books;
        this.offers = that.offers;
        this.sentMessages = that.sentMessages;
        this.receivedMessages = that.receivedMessages;
    }

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