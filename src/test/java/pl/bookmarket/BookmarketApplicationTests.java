package pl.bookmarket;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;
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
import pl.bookmarket.util.ChangeEmailModel;
import pl.bookmarket.util.ChangePasswordModel;
import pl.bookmarket.util.CustomBCryptPasswordEncoder;
import pl.bookmarket.util.ResetPasswordModel;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("mailDisabled")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class BookMarketApplicationTests {

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

        PasswordEncoder encoder = new CustomBCryptPasswordEncoder();

        User user = new User();
        user.setLogin("testUser");
        user.setEmail("testuser@test.pl");
        user.setPassword(encoder.encode("test"));
        user.setRoles(Collections.singleton(role));
        user = userDao.save(user);

        User buyer = new User();
        buyer.setLogin("buyer");
        buyer.setEmail("buyer@test.pl");
        buyer.setPassword(encoder.encode("buyer"));
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
    @WithMockUser
    public void openLoginViewAsAuthenticatedUserReturnForbidden() throws Exception {
        mockMvc.perform(get("/register").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    public void registerNewAccountReturnSuccess() throws Exception {
        User newUser = new User();
        newUser.setLogin("newUser");
        newUser.setEmail("newuser@test.pl");

        mockMvc.perform(post("/register").flashAttr("user", newUser).secure(true).with(csrf().asHeader()))
               .andExpect(status().isCreated())
               .andExpect(model().hasNoErrors())
               .andExpect(view().name("register-success"));
    }

    @Test
    public void registerNewAccountWithInvalidDataReturnUnprocessableEntity() throws Exception {
        User newUser = new User();
        newUser.setLogin("admin");
        newUser.setEmail("superuser");

        mockMvc.perform(post("/register").flashAttr("user", newUser).secure(true).with(csrf().asHeader()))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(model().hasErrors())
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
    public void loginToPortalWithInvalidPasswordReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/login").secure(true)
                                      .param("login", "testUser")
                                      .param("password", "WRONG")
                                      .with(csrf().asHeader()))
               .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void openAdminViewReturnOk() throws Exception {
        mockMvc.perform(get("/admin").secure(true))
               .andExpect(status().isOk())
               .andExpect(view().name("admin"));
    }

    @Test
    @WithMockUser
    public void openAdminViewWithNoAuthorizationReturnDenied() throws Exception {
        mockMvc.perform(get("/admin").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void switchUserAsAdminReturnSuccess() throws Exception {
        mockMvc.perform(get("/impersonate").secure(true).param("username", "testUser"))
               .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void switchUserWithInvalidNameReturnError() throws Exception {
        mockMvc.perform(get("/impersonate").secure(true).param("username", "invalidUser"))
               .andExpect(redirectedUrl("/switchuser"));
    }

    @Test
    @WithMockUser
    public void switchUserWithoutAuthorizationReturnDenied() throws Exception {
        mockMvc.perform(get("/impersonate").secure(true).param("username", "testUser"))
               .andExpect(status().isForbidden());
    }

    @Test
    public void changeLanguageReturnNoContent() throws Exception {
        mockMvc.perform(post("/setlanguage").secure(true).param("lang", "en").with(csrf().asHeader()))
               .andExpect(status().isNoContent());
    }

    @Test
    public void changeLanguageWithInvalidLangParameterReturnBadRequest() throws Exception {
        mockMvc.perform(post("/setlanguage").secure(true).param("lang", "").with(csrf().asHeader()))
               .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void changePasswordReturnSuccess() throws Exception {
        ChangePasswordModel changePasswordModel = new ChangePasswordModel();
        changePasswordModel.setOldPassword("test");
        changePasswordModel.setNewPassword("newPassword123");
        changePasswordModel.setConfirmNewPassword("newPassword123");

        mockMvc.perform(post("/changepassword").secure(true).flashAttr("pass", changePasswordModel)
                                               .with(user("testUser"))
                                               .with(csrf().asHeader()))
               .andExpect(status().isOk())
               .andExpect(view().name("changepassword"))
               .andExpect(model().attribute("success", true))
               .andExpect(model().hasNoErrors());
    }

    @ParameterizedTest
    @MethodSource("changePasswordTestInvalidData")
    public void changePasswordWithInvalidDataReturnErrors(ChangePasswordModel changePasswordModel) throws Exception {
        mockMvc.perform(post("/changepassword").secure(true).flashAttr("pass", changePasswordModel)
                                               .with(user("testUser"))
                                               .with(csrf().asHeader()))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(view().name("changepassword"))
               .andExpect(model().attributeDoesNotExist("success"))
               .andExpect(model().hasErrors());
    }

    private static List<ChangePasswordModel> changePasswordTestInvalidData() {
        List<ChangePasswordModel> testDataList = new ArrayList<>();

        ChangePasswordModel newPasswordMismatch = new ChangePasswordModel("test", "newPassword123", "invalidNewPassword");
        ChangePasswordModel invalidNewPassword = new ChangePasswordModel("test", "newPass", "newPass");
        ChangePasswordModel invalidOldPassword = new ChangePasswordModel("invalid", "newPassword123", "newPassword123");

        testDataList.add(newPasswordMismatch);
        testDataList.add(invalidNewPassword);
        testDataList.add(invalidOldPassword);

        return testDataList;
    }

    @Test
    @Transactional
    public void changeEmailReturnSuccess() throws Exception {
        ChangeEmailModel changeEmailModel = new ChangeEmailModel();
        changeEmailModel.setPassword("test");
        changeEmailModel.setNewEmail("testuser123@test.pl");
        changeEmailModel.setConfirmNewEmail("testuser123@test.pl");

        mockMvc.perform(post("/changeemail").secure(true).flashAttr("mail", changeEmailModel)
                                            .with(user("testUser"))
                                            .with(csrf().asHeader()))
               .andExpect(status().isOk())
               .andExpect(view().name("changeemail"))
               .andExpect(model().attribute("success", true))
               .andExpect(model().hasNoErrors());
    }

    @ParameterizedTest
    @MethodSource("changeEmailInvalidTestData")
    public void changeEmailWithInvalidDataReturnErrors(ChangeEmailModel changeEmailModel) throws Exception {
        mockMvc.perform(post("/changeemail").secure(true).flashAttr("mail", changeEmailModel)
                                            .with(user("testUser"))
                                            .with(csrf().asHeader()))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(view().name("changeemail"))
               .andExpect(model().attributeDoesNotExist("success"))
               .andExpect(model().hasErrors());
    }

    private static List<ChangeEmailModel> changeEmailInvalidTestData() {
        List<ChangeEmailModel> testDataList = new ArrayList<>();

        ChangeEmailModel newEmailMismatch = new ChangeEmailModel("test", "newemail@test.pl", "invalidNewEmail");
        ChangeEmailModel invalidNewEmail = new ChangeEmailModel("test", "newEmail", "newEmail");
        ChangeEmailModel invalidPassword = new ChangeEmailModel("invalid", "newemail@test.pl", "newemail@test.pl");

        testDataList.add(newEmailMismatch);
        testDataList.add(invalidNewEmail);
        testDataList.add(invalidPassword);

        return testDataList;
    }

    @Test
    @Transactional
    public void resetPasswordReturnSuccess() throws Exception {
        ResetPasswordModel resetPasswordModel = new ResetPasswordModel();
        resetPasswordModel.setLogin("testUser");
        resetPasswordModel.setEmail("testuser@test.pl");

        mockMvc.perform(post("/resetpassword").secure(true).flashAttr("resetPassword", resetPasswordModel)
                                              .with(csrf().asHeader()))
               .andExpect(status().isOk())
               .andExpect(view().name("resetpassword"))
               .andExpect(model().attribute("success", true))
               .andExpect(model().hasNoErrors());
    }

    @ParameterizedTest
    @MethodSource("resetPasswordTestData")
    public void resetPasswordWithInvalidDataReturnErrors(ResetPasswordModel resetPasswordModel) throws Exception {
        mockMvc.perform(post("/resetpassword").secure(true).flashAttr("resetPassword", resetPasswordModel)
                                              .with(csrf().asHeader()))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(view().name("resetpassword"))
               .andExpect(model().attributeDoesNotExist("success"))
               .andExpect(model().hasErrors());
    }

    private static List<ResetPasswordModel> resetPasswordTestData() {
        List<ResetPasswordModel> testData = new ArrayList<>();

        ResetPasswordModel invalidLogin = new ResetPasswordModel("invalid", "testuser@test.pl");
        ResetPasswordModel invalidEmail = new ResetPasswordModel("test", "invalid");

        testData.add(invalidLogin);
        testData.add(invalidEmail);

        return testData;
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getUserByIdReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/users/100").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getUserByInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(get("/admin/users/999").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void getUserByIdWithoutAuthorizationReturnForbidden() throws Exception {
        mockMvc.perform(get("/admin/users/100").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void addUserReturnCreated() throws Exception {
        ObjectNode userNode = mapper.createObjectNode();
        userNode.put("login", "user");
        userNode.put("email", "user@test.pl");
        userNode.put("password", "Passw0rd");

        mockMvc.perform(post("/admin/users").secure(true)
                                            .content(mapper.writeValueAsString(userNode))
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .with(csrf().asHeader()))
               .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void addUserWithInvalidDataReturnUnprocessableEntity() throws Exception {
        ObjectNode userNode = mapper.createObjectNode();
        userNode.put("login", "superuser");
        userNode.put("email", "user@test");
        userNode.put("password", "pass");

        mockMvc.perform(post("/admin/users").secure(true)
                                            .content(mapper.writeValueAsString(userNode))
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .with(csrf().asHeader()))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.fieldErrors", hasSize(3)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editUserWithInvalidIdReturnBadRequest() throws Exception {
        String userAsJson = mockMvc.perform(get("/admin/users/100").secure(true))
                                     .andExpect(status().isOk())
                                     .andReturn().getResponse().getContentAsString();

        User user = mapper.readValue(userAsJson, User.class);
        user.setId(999L);

        mockMvc.perform(put("/admin/users/100").secure(true)
                                               .content(mapper.writeValueAsString(user))
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .with(csrf().asHeader()))
               .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @Transactional
    public void deleteUserReturnSuccess() throws Exception {
        mockMvc.perform(delete("/admin/users/100").secure(true).with(csrf().asHeader()))
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
        String getBooksAsJson = mockMvc.perform(get("/api/books").secure(true)
                                                                   .with(user("testUser")))
                                         .andReturn().getResponse().getContentAsString();

        Book book = mapper.treeToValue(mapper.readTree(getBooksAsJson).get(0), Book.class);

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
        String getBooksAsJson = mockMvc.perform(get("/api/books").secure(true)
                                                                   .with(user("testUser")))
                                         .andReturn().getResponse().getContentAsString();

        Book book = mapper.treeToValue(mapper.readTree(getBooksAsJson).get(0), Book.class);

        Offer offer = new Offer();
        offer.setBook(book);
        offer.setComment(new Message());
        offer.getComment().setText("Comment to the offer");

        String offerAsJson = mockMvc.perform(post("/api/market/offers").secure(true)
                                                                         .content(mapper.writeValueAsString(offer))
                                                                         .contentType(MediaType.APPLICATION_JSON)
                                                                         .with(user("buyer"))
                                                                         .with(csrf().asHeader()))
                                      .andExpect(status().isCreated())
                                      .andReturn().getResponse().getContentAsString();

        offer = mapper.readValue(offerAsJson, Offer.class);

        mockMvc.perform(delete("/api/market/offers/{id}", offer.getId()).secure(true)
                                                                        .with(user("buyer"))
                                                                        .with(csrf().asHeader()))
               .andExpect(status().isOk())
               .andExpect(content().string("{}"));
    }

    @Test
    @Transactional
    public void acceptOfferReturnNoContent() throws Exception {
        String getBooksAsJson = mockMvc.perform(get("/api/books").secure(true)
                                                                   .with(user("testUser")))
                                         .andReturn().getResponse().getContentAsString();

        Book book = mapper.treeToValue(mapper.readTree(getBooksAsJson).get(0), Book.class);

        Offer offer = new Offer();
        offer.setBook(book);
        offer.setComment(new Message());
        offer.getComment().setText("Comment to the offer");

        //save offer to get its ID
        String offerAsJson = mockMvc.perform(post("/api/market/offers").secure(true)
                                                                         .content(mapper.writeValueAsString(offer))
                                                                         .contentType(MediaType.APPLICATION_JSON)
                                                                         .with(user("buyer"))
                                                                         .with(csrf().asHeader()))
                                      .andExpect(status().isCreated())
                                      .andReturn().getResponse().getContentAsString();

        offer = mapper.readValue(offerAsJson, Offer.class);

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
    public void getUserLoginsShouldNotContainCurrentUser() {
        List<String> loginList = userDao.getUserLogins("testUser");
        assertFalse(loginList.contains("testUser"));
    }

    @Test
    public void sendMessageReturnCreated() throws Exception {
        User receiver = new User();
        receiver.setLogin("buyer");
        Message message = new Message(null, receiver, StringUtils.repeat("z", 300));

        mockMvc.perform(post("/api/messages").secure(true)
                                             .content(mapper.writeValueAsString(message))
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .with(user("testUser"))
                                             .with(csrf().asHeader()))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.text").value(message.getText()))
               .andExpect(jsonPath("$.sender.login").value("testUser"))
               .andExpect(jsonPath("$.receiver.login").value(message.getReceiver().getLogin()))
               .andExpect(jsonPath("$.read").value(false));
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
    public void sendTooLongMessageReturnUnprocessableEntity() throws Exception {
        User receiver = new User();
        receiver.setLogin("buyer");
        Message message = new Message(null, receiver, StringUtils.repeat("a", 301));

        mockMvc.perform(post("/api/messages").secure(true)
                                             .content(mapper.writeValueAsString(message))
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .with(user("testUser"))
                                             .with(csrf().asHeader()))
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void setMessagesAsReadTest() throws Exception {
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
        List<Long> sentMessagesIdsList = messages.stream().map(Message::getId).collect(Collectors.toList());

        //remove braces from string to get IDs separated by commas
        String sentMessagesIdsString = sentMessagesIdsList.toString().replaceAll("^\\[", "").replaceAll("]$", "");

        mockMvc.perform(put("/api/messages").secure(true)
                                            .param("ids", sentMessagesIdsString)
                                            .with(user("testUser"))
                                            .with(csrf().asHeader()))
               .andExpect(status().isNoContent());

        //get sent messages for testUser and check if all are read
        mockMvc.perform(get("/api/messages").secure(true)
                                            .with(user("testUser")))
               .andExpect(jsonPath("$", hasSize(3)))
               .andExpect(jsonPath("$[0].read").value(true))
               .andExpect(jsonPath("$[1].read").value(true))
               .andExpect(jsonPath("$[2].read").value(true));

        //check if message sent to the other user is not set as read
        mockMvc.perform(get("/api/messages").secure(true)
                                            .with(user("buyer")))
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].read").value(false));
    }
}