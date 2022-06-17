package pl.bookmarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import pl.bookmarket.dto.OfferCreateDto;
import pl.bookmarket.testhelpers.datafactory.AuthenticationFactory;
import pl.bookmarket.testhelpers.datafactory.OfferCreateBuilder;
import pl.bookmarket.testhelpers.datafactory.UserBuilder;
import pl.bookmarket.testhelpers.utils.WithAuthenticatedUser;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.bookmarket.testhelpers.utils.EqualsId.equalsId;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql(value = "/insertAllData.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(value = "/deleteAllData.sql", executionPhase = AFTER_TEST_METHOD)
@WithAuthenticatedUser
class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Value("${bm.controllers.offer}")
    private String offerControllerUrl;

    @Value("${bm.controllers.book}")
    private String bookControllerUrl;

    @Value("${bm.controllers.user}")
    private String userControllerUrl;

    @Test
    @WithAuthenticatedUser(id = 3)
    void shouldSuccessfullyReturnAllBooksForSale() throws Exception {
        mockMvc.perform(get(bookControllerUrl + "/forsale").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(2L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(4L)))));
    }

    @Test
    void shouldSuccessfullyReturnOffersForBookAsBookOwner() throws Exception {
        mockMvc.perform(get(bookControllerUrl + "/2/offers").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].id", equalsId(1L)))
               .andExpect(jsonPath("$[0].book.id", equalsId(2L)))
               .andExpect(jsonPath("$[0].buyer.id", equalsId(2L)));
    }

    @Test
    @WithAuthenticatedUser(id = 3, roles = "ADMIN")
    void shouldSuccessfullyReturnOffersForBookAsAdmin() throws Exception {
        mockMvc.perform(get(bookControllerUrl + "/4/offers").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].id", equalsId(2L)))
               .andExpect(jsonPath("$[0].book.id", equalsId(4L)))
               .andExpect(jsonPath("$[0].buyer.id", equalsId(1L)));
    }

    @Test
    void shouldThrow403WhenGettingOffersForBookOwnedByAnotherUser() throws Exception {
        mockMvc.perform(get(bookControllerUrl + "/1/offers").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldThrow404WhenGettingOffersForNonExistentBook() throws Exception {
        mockMvc.perform(get(bookControllerUrl + "/999/offers").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldSuccessfullyReturnOffersForCurrentUser() throws Exception {
        mockMvc.perform(get(userControllerUrl + "/1/offers").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].id", equalsId(2L)))
               .andExpect(jsonPath("$[0].book.id", equalsId(4L)))
               .andExpect(jsonPath("$[0].buyer.id", equalsId(1L)));
    }

    @Test
    @WithAuthenticatedUser(id = 3, roles = "ADMIN")
    void shouldSuccessfullyReturnOffersForUserAsAdmin() throws Exception {
        mockMvc.perform(get(userControllerUrl + "/1/offers").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].id", equalsId(2L)))
               .andExpect(jsonPath("$[0].book.id", equalsId(4L)))
               .andExpect(jsonPath("$[0].buyer.id", equalsId(1L)));
    }

    @Test
    void shouldThrow403WhenGettingOffersForAnotherUserWithoutPermission() throws Exception {
        mockMvc.perform(get(userControllerUrl + "/2/offers").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldSuccessfullyGetOfferById() throws Exception {
        mockMvc.perform(get(offerControllerUrl + "/2").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)));
    }

    @Test
    @WithAuthenticatedUser(id = 3, roles = "ADMIN")
    void shouldSuccessfullyGetOfferByIdAsAdmin() throws Exception {
        mockMvc.perform(get(offerControllerUrl + "/2").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)));
    }

    @Test
    void shouldThrow404WhenGettingNonExistentOfferById() throws Exception {
        mockMvc.perform(get(offerControllerUrl + "/999").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldThrow403WhenGettingOfferOfAnotherUserById() throws Exception {
        mockMvc.perform(get(offerControllerUrl + "/1").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrow404WhenGettingOffersForNonExistingUser() throws Exception {
        mockMvc.perform(get(userControllerUrl + "/999/offers").secure(true))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.errors[0].field", equalTo("user.id")))
               .andExpect(jsonPath("$.errors[0].errorCode", equalTo("not.found")));
    }

    @Test
    @WithAuthenticatedUser(id = 2)
    @Sql(statements = "UPDATE BOOK SET FOR_SALE = true", scripts = {"/deleteMessages.sql", "/deleteOffers.sql"})
    @SqlMergeMode(MERGE)
    void shouldSuccessfullyAddNewOfferForBook() throws Exception {
        String comment = "Can we exchange books?";
        OfferCreateDto offerCreateDto = new OfferCreateBuilder()
                .withBookId(3L)
                .withComment(comment)
                .build();

        mockMvc.perform(post(offerControllerUrl).secure(true)
                                                .content(mapper.writeValueAsString(offerCreateDto))
                                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", is(notNullValue())))
               .andExpect(jsonPath("$.book.id", equalsId(offerCreateDto.getBookId())))
               .andExpect(jsonPath("$.buyer.id", equalsId(2L)));

        mockMvc.perform(get(userControllerUrl + "/2/messages").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].text", containsString(comment)))
               .andExpect(jsonPath("$[0].sender.id", equalsId(2L)))
               .andExpect(jsonPath("$[0].receiver.id", equalsId(1L)));
    }

    @Test
    void shouldThrow422WhenAddingOfferWithNoBookId() throws Exception {
        OfferCreateDto offerCreateDto = new OfferCreateBuilder().build();

        mockMvc.perform(post(offerControllerUrl).secure(true)
                                                .content(mapper.writeValueAsString(offerCreateDto))
                                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("bookId")))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("errorCode"), equalTo("id.invalid")))));
    }

    @Test
    void shouldThrow404WhenAddingOfferForNonExistentBook() throws Exception {
        OfferCreateDto offerCreateDto = new OfferCreateBuilder()
                .withBookId(999L)
                .build();

        mockMvc.perform(post(offerControllerUrl).secure(true)
                                                .content(mapper.writeValueAsString(offerCreateDto))
                                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldThrow422WhenAddingOfferForBookNotForSale() throws Exception {
        OfferCreateDto offerCreateDto = new OfferCreateBuilder()
                .withBookId(1L)
                .build();

        mockMvc.perform(post(offerControllerUrl).secure(true)
                                                .content(mapper.writeValueAsString(offerCreateDto))
                                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("book")))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("errorCode"), equalTo("book.not.for.sale")))));
    }

    @Test
    void shouldThrow422WhenAddingOfferForOwnBook() throws Exception {
        OfferCreateDto offerCreateDto = new OfferCreateBuilder()
                .withBookId(2L)
                .build();

        mockMvc.perform(post(offerControllerUrl).secure(true)
                                                .content(mapper.writeValueAsString(offerCreateDto))
                                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("book.owner")))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("errorCode"), equalTo("own.book.offer")))));
    }

    @Test
    @Sql(value = "/deleteMessages.sql", executionPhase = BEFORE_TEST_METHOD)
    @SqlMergeMode(MERGE)
    void shouldSuccessfullyAcceptOffer() throws Exception {
        Authentication admin = AuthenticationFactory.getAuthenticationFromUser(UserBuilder.getAdminUser());
        mockMvc.perform(get(offerControllerUrl + "/1").secure(true).with(authentication(admin)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(1L)));
        mockMvc.perform(get(userControllerUrl + "/1/messages").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", is(empty())));
        mockMvc.perform(get(bookControllerUrl + "/2").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)))
               .andExpect(jsonPath("$.owner.id", equalsId(1L)))
               .andExpect(jsonPath("$.forSale", is(true)))
               .andExpect(jsonPath("$.price", is(closeTo(new BigDecimal("8.50"), new BigDecimal("0.00"))), BigDecimal.class));

        mockMvc.perform(post(offerControllerUrl + "/1").secure(true))
               .andExpect(status().isNoContent());

        mockMvc.perform(get(offerControllerUrl + "/1").secure(true))
               .andExpect(status().isNotFound());
        mockMvc.perform(get(userControllerUrl + "/1/messages").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].text", containsString("book.bought")))
               .andExpect(jsonPath("$[0].sender.id", equalsId(1L)))
               .andExpect(jsonPath("$[0].receiver.id", equalsId(2L)));
        mockMvc.perform(get(bookControllerUrl + "/2").secure(true).with(authentication(admin)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)))
               .andExpect(jsonPath("$.owner.id", equalsId(2L)))
               .andExpect(jsonPath("$.forSale", is(false)))
               .andExpect(jsonPath("$.price", is(nullValue())));
    }

    @Test
    void shouldThrow404WhenAcceptingNonExistentOffer() throws Exception {
        mockMvc.perform(post(offerControllerUrl + "/999").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithAuthenticatedUser(id = 3)
    void shouldThrow403WhenAcceptingOfferForBookOfAnotherUser() throws Exception {
        mockMvc.perform(post(offerControllerUrl + "/1").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldSuccessfullyDeleteOffer() throws Exception {
        mockMvc.perform(get(offerControllerUrl + "/2").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)));

        mockMvc.perform(delete(offerControllerUrl + "/2").secure(true))
               .andExpect(status().isNoContent());

        mockMvc.perform(get(offerControllerUrl + "/2").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithAuthenticatedUser(id = 3, roles = "ADMIN")
    void shouldSuccessfullyDeleteOfferAsAdmin() throws Exception {
        mockMvc.perform(get(bookControllerUrl + "/2/offers").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete(offerControllerUrl + "/1").secure(true))
               .andExpect(status().isNoContent());

        mockMvc.perform(get(bookControllerUrl + "/2/offers").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", is(empty())));
    }

    @Test
    void shouldThrow404WhenDeletingNonExistentOffer() throws Exception {
        mockMvc.perform(delete(offerControllerUrl + "/999").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldThrow403WhenDeletingOfferForAnotherUser() throws Exception {
        mockMvc.perform(delete(offerControllerUrl + "/1").secure(true))
               .andExpect(status().isForbidden());
    }
}