package pl.bookmarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import pl.bookmarket.dto.BookCreateDto;
import pl.bookmarket.model.Genre;
import pl.bookmarket.testhelpers.datafactory.BookBuilder;
import pl.bookmarket.testhelpers.utils.WithAuthenticatedUser;
import pl.bookmarket.util.ApplicationProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.bookmarket.testhelpers.utils.EqualsId.equalsId;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql(value = "/insertAllData.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(value = "/deleteAllData.sql", executionPhase = AFTER_TEST_METHOD)
@WithAuthenticatedUser
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ApplicationProperties properties;

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldSuccessfullyReturnAllBooks() throws Exception {
        mockMvc.perform(get(properties.getBooksApiUrl()).secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(4)))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(1L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(2L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(3L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(4L)))));
    }

    @Test
    void shouldThrow403WhenGettingAllBooksWithoutPermission() throws Exception {
        mockMvc.perform(get(properties.getBooksApiUrl()).secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldSuccessfullyGetBookById() throws Exception {
        mockMvc.perform(get(properties.getBooksApiUrl() + "/2").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)))
               .andExpect(jsonPath("$.title", equalTo("Harry Potter and the Prisoner of Azkaban")))
               .andExpect(jsonPath("$.author", equalTo("J.K. Rowling")))
               .andExpect(jsonPath("$.pages", equalTo(452)))
               .andExpect(jsonPath("$.publisher", equalTo("Test Publisher 2")))
               .andExpect(jsonPath("$.releaseYear", equalTo(2000)))
               .andExpect(jsonPath("$.forSale", equalTo(true)))
               .andExpect(jsonPath("$.price", closeTo(new BigDecimal("8.50"), new BigDecimal("0.00")), BigDecimal.class))
               .andExpect(jsonPath("$.genre.name", equalTo("Fantasy")))
               .andExpect(jsonPath("$.owner.id", equalsId(1L)));
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldSuccessfullyGetBookByIdAsAdmin() throws Exception {
        mockMvc.perform(get(properties.getBooksApiUrl() + "/1").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(1L)));
    }

    @Test
    void shouldThrow403WhenGettingBookOfAnotherUserWithoutPermission() throws Exception {
        mockMvc.perform(get(properties.getBooksApiUrl() + "/1").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldThrow404WhenGettingNonExistingBook() throws Exception {
        mockMvc.perform(get(properties.getBooksApiUrl() + "/999").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldSuccessfullyGetBooksByOwnerId() throws Exception {
        mockMvc.perform(get(properties.getUsersApiUrl() + "/1/books").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(2L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(3L)))));
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldSuccessfullyGetBooksByOwnerIdAsAdmin() throws Exception {
        mockMvc.perform(get(properties.getUsersApiUrl() + "/2/books").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(1L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(4L)))));
    }

    @Test
    void shouldThrow403WhenGettingBooksForAnotherUserWithoutPermission() throws Exception {
        mockMvc.perform(get(properties.getUsersApiUrl() + "/2/books").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldReturnEmptyListWhenGettingBooksForNonExistentUser() throws Exception {
        mockMvc.perform(get(properties.getUsersApiUrl() + "/999/books").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", is(empty())));
    }

    @ParameterizedTest
    @MethodSource("getValidYears")
    void shouldSuccessfullyCreateNewBookWithDifferentReleaseYears(Integer releaseYear) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withReleaseYear(releaseYear).buildBookCreateDto();

        mockMvc.perform(post(properties.getBooksApiUrl()).secure(true)
                                                         .content(mapper.writeValueAsString(bookCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", is(notNullValue())))
               .andExpect(jsonPath("$.title", equalTo(bookCreateDto.getTitle())))
               .andExpect(jsonPath("$.author", equalTo(bookCreateDto.getAuthor())))
               .andExpect(jsonPath("$.pages", equalTo(bookCreateDto.getPages())))
               .andExpect(jsonPath("$.publisher", equalTo(bookCreateDto.getPublisher())))
               .andExpect(jsonPath("$.releaseYear", equalTo(bookCreateDto.getReleaseYear())))
               .andExpect(jsonPath("$.forSale", equalTo(bookCreateDto.isForSale())))
               .andExpect(jsonPath("$.price", equalTo(bookCreateDto.getPrice())))
               .andExpect(jsonPath("$.genre.id", equalsId(bookCreateDto.getGenreId())))
               .andExpect(jsonPath("$.genre.name", is(notNullValue())))
               .andExpect(jsonPath("$.owner.id", equalsId(1L)));
    }

    @ParameterizedTest
    @MethodSource("getValidPrices")
    void shouldSuccessfullyCreateNewBookWithDifferentPrices(BigDecimal price) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withForSale(true).withPrice(price)
                                                 .buildBookCreateDto();

        mockMvc.perform(post(properties.getBooksApiUrl()).secure(true)
                                                         .content(mapper.writeValueAsString(bookCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", is(notNullValue())))
               .andExpect(jsonPath("$.releaseYear", equalTo(bookCreateDto.getReleaseYear())))
               .andExpect(jsonPath("$.forSale", equalTo(bookCreateDto.isForSale())))
               .andExpect(jsonPath("$.price", equalTo(bookCreateDto.getPrice()), BigDecimal.class))
               .andExpect(jsonPath("$.genre.id", equalsId(bookCreateDto.getGenreId())))
               .andExpect(jsonPath("$.genre.name", is(notNullValue())))
               .andExpect(jsonPath("$.owner.id", equalsId(1L)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrow422WhenCreatingNewBookWithInvalidTitle(String title) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withTitle(title).buildBookCreateDto();

        mockMvc.perform(post(properties.getBooksApiUrl()).secure(true)
                                                         .content(mapper.writeValueAsString(bookCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "title"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("errorCode", "field.blank"))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrow422WhenCreatingNewBookWithInvalidAuthor(String author) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withAuthor(author).buildBookCreateDto();

        mockMvc.perform(post(properties.getBooksApiUrl()).secure(true)
                                                         .content(mapper.writeValueAsString(bookCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "author"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("errorCode", "field.blank"))));
    }

    @Test
    void shouldThrow422WhenCreatingNewBookWithInvalidGenreId() throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withGenre(null).buildBookCreateDto();

        mockMvc.perform(post(properties.getBooksApiUrl()).secure(true)
                                                         .content(mapper.writeValueAsString(bookCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "genreId"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("errorCode", "id.invalid"))));
    }

    @Test
    void shouldThrow404WhenCreatingNewBookWithNonExistentGenreId() throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withGenre(new Genre(999L, "")).buildBookCreateDto();

        mockMvc.perform(post(properties.getBooksApiUrl()).secure(true)
                                                         .content(mapper.writeValueAsString(bookCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "genre.id"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("errorCode", "not.found"))));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1, -5})
    void shouldThrow422WhenCreatingNewBookWithInvalidNumberOfPages(Integer pages) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withPages(pages).buildBookCreateDto();

        mockMvc.perform(post(properties.getBooksApiUrl()).secure(true)
                                                         .content(mapper.writeValueAsString(bookCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "pages"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("errorCode"), oneOf("field.blank", "field.not.positive.value")))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrow422WhenCreatingNewBookWithInvalidPublisher(String publisher) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withPublisher(publisher).buildBookCreateDto();

        mockMvc.perform(post(properties.getBooksApiUrl()).secure(true)
                                                         .content(mapper.writeValueAsString(bookCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "publisher"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("errorCode", "field.blank"))));
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidYears")
    void shouldThrow422WhenCreatingNewBookWithInvalidReleaseYear(Integer releaseYear) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withReleaseYear(releaseYear).buildBookCreateDto();

        mockMvc.perform(post(properties.getBooksApiUrl()).secure(true)
                                                         .content(mapper.writeValueAsString(bookCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "releaseYear"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("errorCode"), oneOf("field.blank", "year.too.low", "year.too.high")))));
    }

    @ParameterizedTest
    @MethodSource("getInvalidPrices")
    void shouldThrow422WhenCreatingNewBookWithInvalidPrice(BigDecimal price) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withForSale(true).withPrice(price)
                                                 .buildBookCreateDto();

        mockMvc.perform(post(properties.getBooksApiUrl()).secure(true)
                                                         .content(mapper.writeValueAsString(bookCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", either(hasSize(1)).or(hasSize(2))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "price"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("errorCode"), oneOf("price.negative", "price.invalid.precision")))));
    }

    @ParameterizedTest
    @MethodSource("getInvalidForSaleStatus")
    void shouldThrow422WhenCreatingNewBookWithInvalidForSaleStatus(boolean forSale, BigDecimal price) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withForSale(forSale).withPrice(price)
                                                 .buildBookCreateDto();

        mockMvc.perform(post(properties.getBooksApiUrl()).secure(true)
                                                         .content(mapper.writeValueAsString(bookCreateDto))
                                                         .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "forSale"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("errorCode"), equalTo("book.status.invalid")))));
    }

    @ParameterizedTest
    @MethodSource("getValidYears")
    void shouldSuccessfullyUpdateBookWithDifferentReleaseYears(Integer releaseYear) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withReleaseYear(releaseYear).buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)))
               .andExpect(jsonPath("$.title", equalTo(bookCreateDto.getTitle())))
               .andExpect(jsonPath("$.author", equalTo(bookCreateDto.getAuthor())))
               .andExpect(jsonPath("$.pages", equalTo(bookCreateDto.getPages())))
               .andExpect(jsonPath("$.publisher", equalTo(bookCreateDto.getPublisher())))
               .andExpect(jsonPath("$.releaseYear", equalTo(bookCreateDto.getReleaseYear())))
               .andExpect(jsonPath("$.forSale", equalTo(bookCreateDto.isForSale())))
               .andExpect(jsonPath("$.price", equalTo(bookCreateDto.getPrice())))
               .andExpect(jsonPath("$.genre.id", equalsId(bookCreateDto.getGenreId())))
               .andExpect(jsonPath("$.genre.name", is(notNullValue())))
               .andExpect(jsonPath("$.owner.id", equalsId(1L)));
    }

    @ParameterizedTest
    @MethodSource("getValidPrices")
    void shouldSuccessfullyUpdateBookWithDifferentPrices(BigDecimal price) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withForSale(true).withPrice(price)
                                                 .buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(2L)))
               .andExpect(jsonPath("$.releaseYear", equalTo(bookCreateDto.getReleaseYear())))
               .andExpect(jsonPath("$.forSale", equalTo(bookCreateDto.isForSale())))
               .andExpect(jsonPath("$.price", equalTo(bookCreateDto.getPrice()), BigDecimal.class))
               .andExpect(jsonPath("$.genre.id", equalsId(bookCreateDto.getGenreId())))
               .andExpect(jsonPath("$.genre.name", is(notNullValue())))
               .andExpect(jsonPath("$.owner.id", equalsId(1L)));
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldSuccessfullyUpdateAnotherUsersBookAsAdmin() throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/1").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(1L)))
               .andExpect(jsonPath("$.title", equalTo(bookCreateDto.getTitle())))
               .andExpect(jsonPath("$.author", equalTo(bookCreateDto.getAuthor())))
               .andExpect(jsonPath("$.pages", equalTo(bookCreateDto.getPages())))
               .andExpect(jsonPath("$.publisher", equalTo(bookCreateDto.getPublisher())))
               .andExpect(jsonPath("$.releaseYear", equalTo(bookCreateDto.getReleaseYear())))
               .andExpect(jsonPath("$.forSale", equalTo(bookCreateDto.isForSale())))
               .andExpect(jsonPath("$.price", equalTo(bookCreateDto.getPrice())))
               .andExpect(jsonPath("$.genre.id", equalsId(bookCreateDto.getGenreId())))
               .andExpect(jsonPath("$.genre.name", is(notNullValue())))
               .andExpect(jsonPath("$.owner.id", equalsId(2L)));
    }

    @Test
    void shouldRemoveAllOffersForBookThatChangedForSaleStatus() throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook()
                                                 .withId(2L)
                                                 .withForSale(false)
                                                 .withPrice(null)
                                                 .buildBookCreateDto();
        mockMvc.perform(get(properties.getBooksApiUrl() + "/2/offers").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].book.id", equalsId(2L)));

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.forSale", is(false)))
               .andExpect(jsonPath("$.price", nullValue()));

        mockMvc.perform(get(properties.getBooksApiUrl() + "/2/offers").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", empty()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrow422WhenUpdatingBookWithInvalidTitle(String title) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withTitle(title).buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "title"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("errorCode", "field.blank"))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrow422WhenUpdatingBookWithInvalidAuthor(String author) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withAuthor(author).buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "author"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("errorCode", "field.blank"))));
    }

    @Test
    void shouldThrow422WhenUpdatingBookWithInvalidGenreId() throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withGenre(null).buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "genreId"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("errorCode", "id.invalid"))));
    }

    @Test
    void shouldThrow404WhenUpdatingBookWithNonExistentGenreId() throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withGenre(new Genre(999L, "")).buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "genre.id"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("errorCode", "not.found"))));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1, -5})
    void shouldThrow422WhenUpdatingBookWithInvalidNumberOfPages(Integer pages) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withPages(pages).buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "pages"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("errorCode"), oneOf("field.blank", "field.not.positive.value")))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrow422WhenUpdatingBookWithInvalidPublisher(String publisher) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withPublisher(publisher).buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "publisher"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("errorCode", "field.blank"))));
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidYears")
    void shouldThrow422WhenUpdatingBookWithInvalidReleaseYear(Integer releaseYear) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withReleaseYear(releaseYear).buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "releaseYear"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("errorCode"), oneOf("field.blank", "year.too.low", "year.too.high")))));
    }

    @ParameterizedTest
    @MethodSource("getInvalidPrices")
    void shouldThrow422WhenUpdatingBookWithInvalidPrice(BigDecimal price) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withForSale(true).withPrice(price)
                                                 .buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", either(hasSize(1)).or(hasSize(2))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "price"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("errorCode"), oneOf("price.negative", "price.invalid.precision")))));
    }

    @ParameterizedTest
    @MethodSource("getInvalidForSaleStatus")
    void shouldThrow422WhenUpdatingBookWithInvalidForSaleStatus(boolean forSale, BigDecimal price) throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().withForSale(forSale).withPrice(price)
                                                 .buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/2").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry("field", "forSale"))))
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("errorCode"), equalTo("book.status.invalid")))));
    }

    @Test
    void shouldThrow404WhenUpdatingNonExistentBook() throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/999").secure(true)
                                                                 .content(mapper.writeValueAsString(bookCreateDto))
                                                                 .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldThrow403WhenUpdatingAnotherUsersBookWithoutPermission() throws Exception {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().buildBookCreateDto();

        mockMvc.perform(put(properties.getBooksApiUrl() + "/1").secure(true)
                                                               .content(mapper.writeValueAsString(bookCreateDto))
                                                               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isForbidden());
    }

    @Test
    void shouldSuccessfullyDeleteBook() throws Exception {
        mockMvc.perform(delete(properties.getBooksApiUrl() + "/2").secure(true))
               .andExpect(status().isNoContent());
        mockMvc.perform(get(properties.getBooksApiUrl() + "/2").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldSuccessfullyDeleteBookAsAdmin() throws Exception {
        mockMvc.perform(delete(properties.getBooksApiUrl() + "/1").secure(true))
               .andExpect(status().isNoContent());
        mockMvc.perform(get(properties.getBooksApiUrl() + "/1").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrow404WhenDeletingNonExistentBook() throws Exception {
        mockMvc.perform(delete(properties.getBooksApiUrl() + "/999").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldThrow403WhenDeletingAnotherUsersBookWithoutPermission() throws Exception {
        mockMvc.perform(delete(properties.getBooksApiUrl() + "/1").secure(true))
               .andExpect(status().isForbidden());
    }

    private static Stream<String> getValidPrices() {
        return Stream.of("5", "8.2", "27.55", "100", "99999.99");
    }

    private static Stream<Integer> getValidYears() {
        return Stream.of(1500, LocalDate.now(ZoneId.systemDefault()).getYear());
    }

    private static Stream<String> getInvalidPrices() {
        return Stream.of("-1.00", "-15.3", "-10", "-7.999", "5.123", "1567.890", "100000");
    }

    private static Stream<Integer> getInvalidYears() {
        return Stream.of(1499, LocalDate.now(ZoneId.systemDefault()).getYear() + 1);
    }

    private static Stream<Arguments> getInvalidForSaleStatus() {
        return Stream.of(
                Arguments.of(true, null),
                Arguments.of(false, "10.00")
        );
    }
}