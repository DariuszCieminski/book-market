package pl.bookmarket;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.bookmarket.dao.BookDao;
import pl.bookmarket.dao.GenreDao;
import pl.bookmarket.dao.MessageDao;
import pl.bookmarket.dao.RoleDao;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.Genre;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.Offer;
import pl.bookmarket.model.Role;
import pl.bookmarket.model.User;
import pl.bookmarket.util.CustomPasswordEncoder;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class BookmarketApplicationTests {

    static final UserDetails USER =
        new org.springframework.security.core.userdetails.User(
            "user", "user", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    static final UserDetails ADMIN =
        new org.springframework.security.core.userdetails.User(
            "admin", "admin",
            Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN")));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private BookDao bookDao;

    @Autowired
    private GenreDao genreDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private ObjectMapper mapper;

    @BeforeAll
    public void init() {
        Role role = new Role();
        role.setName("USER");
        role = roleDao.save(role);

        User user = new User();
        user.setLogin("testUser");
        user.setEmail("test-user@test.pl");
        user.setPassword(CustomPasswordEncoder.hash("test"));
        user.setRoles(Collections.singleton(role));
        user = userDao.save(user);

        User buyer = new User();
        buyer.setLogin("buyer");
        buyer.setEmail("buyer@test.pl");
        buyer.setPassword(CustomPasswordEncoder.hash("buyer"));
        userDao.save(buyer);

        Genre genre = new Genre();
        genre.setName("Test");
        genre = genreDao.save(genre);

        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setReleaseYear(2000);
        book.setPages(200);
        book.setPublisher("Publisher");
        book.setGenre(genre);
        book.setForSale(true);
        book.setOwner(user);
        bookDao.save(book);
    }

    @Test
    public void openRegisterViewReturnOk() throws Exception {
        mockMvc.perform(get("/register").secure(true))
               .andExpect(status().isOk())
               .andExpect(view().name("register"));
    }

    @Test
    public void openLoginViewAsAuthenticatedUserReturnForbidden() throws Exception {
        mockMvc.perform(get("/register").secure(true)
                                        .with(user(USER)))
               .andExpect(status().isForbidden());
    }

    @Test
    public void registerNewAccountReturnSuccess() throws Exception {
        pl.bookmarket.model.User newUser = new pl.bookmarket.model.User();
        newUser.setLogin("newUser");
        newUser.setEmail("newuser@test.pl");
        mockMvc.perform(post("/register").flashAttr("user", newUser).secure(true)
                                         .with(csrf().asHeader()))
               .andExpect(status().isOk())
               .andExpect(view().name("register-success"));
    }

    @Test
    public void registerNewAccountWithInvalidDataReturnUnprocessableEntity() throws Exception {
        pl.bookmarket.model.User newUser = new pl.bookmarket.model.User();
        newUser.setLogin("admin");
        newUser.setEmail("superuser");
        mockMvc.perform(post("/register").flashAttr("user", newUser).secure(true)
                                         .with(csrf().asHeader()))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(view().name("register"));
    }

    @Test
    public void loginToPortalReturnSuccess() throws Exception {
        mockMvc.perform(post("/login").secure(true)
                                      .param("login", "testUser")
                                      .param("password", "test")
                                      .with(csrf().asHeader()))
               .andExpect(redirectedUrl("/"));
    }

    @Test
    public void loginToPortalWithInvalidPasswordExpectUnauthorized() throws Exception {
        mockMvc.perform(post("/login").secure(true)
                                      .param("login", "testUser")
                                      .param("password", "WRONG")
                                      .with(csrf().asHeader()))
               .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void openAdminViewReturnOk() throws Exception {
        mockMvc.perform(get("/admin").secure(true)
                                     .with(user(ADMIN)))
               .andExpect(status().isOk())
               .andExpect(view().name("admin"));
    }

    @Test
    public void openAdminViewWithNoAuthorizationReturnDenied() throws Exception {
        mockMvc.perform(get("/admin").secure(true)
                                     .with(user(USER)))
               .andExpect(status().isForbidden());
    }

    @Test
    public void switchUserAsAdminReturnSuccess() throws Exception {
        mockMvc.perform(get("/impersonate").secure(true)
                                           .param("username", "testUser")
                                           .with(user(ADMIN)))
               .andExpect(redirectedUrl("/"));
    }

    @Test
    public void switchUserWithoutAuthorizationReturnDenied() throws Exception {
        mockMvc.perform(get("/impersonate").secure(true)
                                           .param("username", "testUser")
                                           .with(user(USER)))
               .andExpect(status().isForbidden());
    }

    @Test
    public void changeLanguageReturnNoContent() throws Exception {
        mockMvc.perform(post("/setlanguage").secure(true)
                                            .param("lang", "en")
                                            .with(csrf().asHeader()))
               .andExpect(status().isNoContent());
    }

    @Test
    public void changeLanguageWithInvalidLangParameterReturnBadRequest() throws Exception {
        mockMvc.perform(post("/setlanguage").secure(true)
                                            .param("lang", "")
                                            .with(csrf().asHeader()))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getUserByIdReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/users/100").secure(true)
                                               .with(user(ADMIN)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    public void getUserByInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(get("/admin/users/999").secure(true)
                                               .with(user(ADMIN)))
               .andExpect(status().isNotFound());
    }

    @Test
    public void getUserByIdWithoutAuthorizationReturnForbidden() throws Exception {
        mockMvc.perform(get("/admin/users/100").secure(true)
                                               .with(user(USER)))
               .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void addUserReturnCreated() throws Exception {
        ObjectNode userNode = mapper.createObjectNode();
        userNode.put("login", "user");
        userNode.put("email", "user@test.pl");
        userNode.put("password", "Passw0rd");
        mockMvc.perform(post("/admin/users").secure(true)
                                            .content(mapper.writeValueAsString(userNode))
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .with(user(ADMIN))
                                            .with(csrf().asHeader()))
               .andExpect(status().isCreated());
    }

    @Test
    public void addUserWithInvalidDataReturnUnprocessableEntity() throws Exception {
        ObjectNode userNode = mapper.createObjectNode();
        userNode.put("login", "superuser");
        userNode.put("email", "user@test");
        userNode.put("password", "pass");
        mockMvc.perform(post("/admin/users").secure(true)
                                            .content(mapper.writeValueAsString(userNode))
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .with(user(ADMIN))
                                            .with(csrf().asHeader()))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.fieldErrors", hasSize(3)));
    }

    @Test
    public void editUserWithInvalidIdReturnBadRequest() throws Exception {
        String userAsString = mockMvc.perform(get("/admin/users/100").secure(true)
                                                                     .with(user(ADMIN)))
                                     .andExpect(status().isOk())
                                     .andReturn().getResponse().getContentAsString();
        User user = mapper.readValue(userAsString, User.class);
        user.setId(999L);

        mockMvc.perform(put("/admin/users/100").secure(true)
                                               .content(mapper.writeValueAsString(user))
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .with(user(ADMIN))
                                               .with(csrf().asHeader()))
               .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void deleteUserReturnSuccess() throws Exception {
        mockMvc.perform(delete("/admin/users/100").secure(true)
                                                  .with(user(ADMIN))
                                                  .with(csrf().asHeader()))
               .andExpect(status().isOk())
               .andExpect(content().string("{}"));
    }

    @Test
    public void addBookReturnCreated() throws Exception {
        Book book = new Book();
        book.setTitle("Test book");
        book.setAuthor("Test author");
        book.setGenre(new Genre());
        book.getGenre().setId(1L);
        book.setForSale(true);
        book.setPrice(BigDecimal.TEN);
        book.setPages(300);
        book.setPublisher("Test publisher");
        book.setReleaseYear(2015);

        mockMvc.perform(post("/api/books").secure(true)
                                          .content(mapper.writeValueAsString(book)).contentType(MediaType.APPLICATION_JSON)
                                          .with(user("testUser"))
                                          .with(csrf().asHeader()))
               .andExpect(status().isCreated());
    }

    @Test
    public void addBookWithInvalidDataReturnUnprocessableEntity() throws Exception {
        Book book = new Book();
        book.setTitle("");
        book.setAuthor("");
        book.setPublisher("");
        book.setGenre(null);
        book.setPages(-10);
        book.setReleaseYear(-1000);
        book.setForSale(true);
        book.setPrice(BigDecimal.valueOf(-19.999D));

        mockMvc.perform(post("/api/books").secure(true)
                                          .content(mapper.writeValueAsString(book)).contentType(MediaType.APPLICATION_JSON)
                                          .with(user("testUser"))
                                          .with(csrf().asHeader()))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.fieldErrors", hasSize(8)));
    }

    @Test
    public void editBookWithNoIdReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/books/999").secure(true)
                                             .content(mapper.writeValueAsString(new Book()))
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .with(user("testUser"))
                                             .with(csrf().asHeader()))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(containsString("ID mismatch")));
    }

    @Test
    public void editBookWithInvalidIdReturnNotFound() throws Exception {
        Book book = new Book();
        book.setId(333L);
        book.setGenre(new Genre());
        book.setAuthor("author");
        book.setTitle("title");
        book.setPublisher("publisher");
        book.setPages(100);
        book.setReleaseYear(2010);

        mockMvc.perform(put("/api/books/333").secure(true)
                                             .content(mapper.writeValueAsString(book))
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .with(user("testUser"))
                                             .with(csrf().asHeader()))
               .andExpect(status().isNotFound())
               .andExpect(content().string(containsString("does not exist")));
    }

    @Test
    public void getBooksForUserReturnOk() throws Exception {
        mockMvc.perform(get("/api/books").secure(true)
                                         .with(user("testUser")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Transactional
    public void addOfferReturnCreated() throws Exception {
        //get book
        String getBooksResponse = mockMvc.perform(get("/api/books").secure(true)
                                                                   .with(user("testUser")))
                                         .andReturn().getResponse().getContentAsString();

        Book book = mapper.treeToValue(mapper.readTree(getBooksResponse).get(0), Book.class);

        //make offer
        Offer offer = new Offer();
        offer.setBook(book);
        offer.setComment(new Message());
        offer.getComment().setText("Comment to the offer");

        mockMvc.perform(post("/api/market/offers").secure(true)
                                                  .content(mapper.writeValueAsString(offer))
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .with(user("buyer"))
                                                  .with(csrf().asHeader()))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.buyer.login").value("buyer"))
               .andExpect(jsonPath("$.book.owner.login").value("testUser"))
               .andExpect(jsonPath("$.book.id").value(book.getId()));
    }

    @Test
    @Transactional
    public void deleteOfferReturnOk() throws Exception {
        String getBooksResponse = mockMvc.perform(get("/api/books").secure(true)
                                                                   .with(user("testUser")))
                                         .andReturn().getResponse().getContentAsString();

        Book book = mapper.treeToValue(mapper.readTree(getBooksResponse).get(0), Book.class);

        Offer offer = new Offer();
        offer.setBook(book);
        offer.setComment(new Message());
        offer.getComment().setText("Comment to the offer");

        String offerAsString = mockMvc.perform(post("/api/market/offers").secure(true)
                                                                         .content(mapper.writeValueAsString(offer))
                                                                         .contentType(MediaType.APPLICATION_JSON)
                                                                         .with(user("buyer"))
                                                                         .with(csrf().asHeader()))
                                      .andExpect(status().isCreated())
                                      .andReturn().getResponse().getContentAsString();

        offer = mapper.readValue(offerAsString, Offer.class);

        mockMvc.perform(delete("/api/market/offers/{id}", offer.getId()).secure(true)
                                                                        .with(user("buyer"))
                                                                        .with(csrf().asHeader()))
               .andExpect(status().isOk())
               .andExpect(content().string("{}"));
    }

    @Test
    @Transactional
    public void acceptOfferReturnNoContent() throws Exception {
        String getBooksResponse = mockMvc.perform(get("/api/books").secure(true)
                                                                   .with(user("testUser")))
                                         .andReturn().getResponse().getContentAsString();

        Book book = mapper.treeToValue(mapper.readTree(getBooksResponse).get(0), Book.class);

        Offer offer = new Offer();
        offer.setBook(book);
        offer.setComment(new Message());
        offer.getComment().setText("Comment to the offer");

        //save offer to get its ID
        String offerAsString = mockMvc.perform(post("/api/market/offers").secure(true)
                                                                         .content(mapper.writeValueAsString(offer))
                                                                         .contentType(MediaType.APPLICATION_JSON)
                                                                         .with(user("buyer"))
                                                                         .with(csrf().asHeader()))
                                      .andExpect(status().isCreated())
                                      .andReturn().getResponse().getContentAsString();

        offer = mapper.readValue(offerAsString, Offer.class);

        mockMvc.perform(post("/api/market/offers/accept").secure(true)
                                                         .content(mapper.writeValueAsString(offer))
                                                         .contentType(MediaType.APPLICATION_JSON)
                                                         .with(user("testUser"))
                                                         .with(csrf().asHeader()))
               .andExpect(status().isNoContent());
    }

    @Test
    public void acceptOfferWithoutIdReturnUnprocessableEntity() throws Exception {
        mockMvc.perform(post("/api/market/offers/accept").secure(true)
                                                         .content(mapper.writeValueAsString(new Offer()))
                                                         .contentType(MediaType.APPLICATION_JSON)
                                                         .with(user("testUser"))
                                                         .with(csrf().asHeader()))
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Transactional
    public void sendMessageReturnCreated() throws Exception {
        User receiver = new User();
        receiver.setLogin("buyer");
        Message message = new Message(null, receiver, "This is a test message.");

        mockMvc.perform(post("/api/messages").secure(true)
                                             .content(mapper.writeValueAsString(message))
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .with(user("testUser"))
                                             .with(csrf().asHeader()))
               .andExpect(status().isCreated());

        String unreadMessagesAsString = mockMvc.perform(get("/api/messages/unread").secure(true)
                                                                                   .with(user("buyer")))
                                               .andExpect(jsonPath("$").isArray())
                                               .andExpect(jsonPath("$", hasSize(1)))
                                               .andReturn().getResponse().getContentAsString();

        //check if sent message is valid
        Message sentMessage = mapper.treeToValue(mapper.readTree(unreadMessagesAsString).get(0), Message.class);

        Assertions.assertEquals("This is a test message.", sentMessage.getText());
        Assertions.assertEquals("buyer", sentMessage.getReceiver().getLogin());
        Assertions.assertEquals("testUser", sentMessage.getSender().getLogin());
        Assertions.assertFalse(sentMessage.isRead());
    }

    @Test
    public void sendMessageWithInvalidReceiverReturnUnprocessableEntity() throws Exception {
        mockMvc.perform(post("/api/messages").secure(true)
                                             .content(mapper.writeValueAsString(new Message()))
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .with(user("testUser"))
                                             .with(csrf().asHeader()))
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void getUserLoginsShouldNotContainCurrentUser() {
        User currentUser = userDao.findUserByLogin("testUser");
        List<String> loginList = userDao.getUserLogins(currentUser.getLogin());

        Assertions.assertFalse(loginList.contains(currentUser.getLogin()));
    }

    @Test
    @Transactional
    public void setMessagesReadTestMethod() throws Exception {
        //create few messages
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            messages.add(new Message(userDao.findUserByLogin("buyer"), userDao.findUserByLogin("testUser"), "Message"));
        }

        //add message to the other user, which later should not be set as read
        messages.add(new Message(userDao.findUserByLogin("testUser"), userDao.findUserByLogin("buyer"), "Message"));

        //send messages to users
        messages = (List<Message>) messageDao.saveAll(messages);

        //get IDs of sent messages
        List<Long> sentMessagesIds = messages.stream().map(Message::getId).collect(Collectors.toList());

        //prepare data for request
        String requestData = sentMessagesIds.toString().replaceAll("^\\[", "").replaceAll("]$", "");

        mockMvc.perform(put("/api/messages").secure(true)
                                            .param("ids", requestData)
                                            .with(user("testUser"))
                                            .with(csrf().asHeader()))
               .andExpect(status().isNoContent());

        //get sent messages for testUser and check if all are read
        String sentMessagesAsString = mockMvc.perform(get("/api/messages").secure(true)
                                                                          .with(user("testUser")))
                                             .andExpect(jsonPath("$", hasSize(3)))
                                             .andReturn().getResponse().getContentAsString();

        List<Message> sentMessagesAsList = mapper
            .readValue(sentMessagesAsString, mapper.getTypeFactory().constructCollectionType(List.class, Message.class));

        for (Message message : sentMessagesAsList) {
            Assertions.assertTrue(message.isRead());
        }

        //check if message sent to the other user is not set as read
        mockMvc.perform(get("/api/messages").secure(true)
                                            .with(user("buyer")))
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].read").value(false));
    }
}