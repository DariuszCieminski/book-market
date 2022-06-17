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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import pl.bookmarket.dto.GenreDto;
import pl.bookmarket.testhelpers.utils.WithAuthenticatedUser;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE;
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
@Sql(value = "/insertGenres.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(value = "/deleteGenres.sql", executionPhase = AFTER_TEST_METHOD)
@WithAuthenticatedUser
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Value("${bm.controllers.genre}")
    private String genreControllerUrl;

    @Test
    void shouldSuccessfullyGetAllGenres() throws Exception {
        mockMvc.perform(get(genreControllerUrl).secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(7)))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(1L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(2L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(3L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(4L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(5L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(6L)))))
               .andExpect(jsonPath("$", hasItem(hasEntry(equalTo("id"), equalsId(7L)))));
    }

    @Test
    void shouldSuccessfullyGetGenreById() throws Exception {
        mockMvc.perform(get(genreControllerUrl + "/1").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(1L)))
               .andExpect(jsonPath("$.name", equalTo("Novel")));
    }

    @Test
    void shouldThrow404WhenGettingNonExistingGenreById() throws Exception {
        mockMvc.perform(get(genreControllerUrl + "/999").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    @Sql(value = "/deleteGenres.sql", executionPhase = BEFORE_TEST_METHOD)
    @SqlMergeMode(MERGE)
    void shouldSuccessfullyCreateNewGenre() throws Exception {
        GenreDto genreDto = new GenreDto();
        genreDto.setName("Cooking");

        mockMvc.perform(post(genreControllerUrl).secure(true)
                                                .content(mapper.writeValueAsString(genreDto))
                                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", equalsId(1L)))
               .andExpect(jsonPath("$.name", equalTo(genreDto.getName())));
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrow422WhenCreatingGenreWithExistingName() throws Exception {
        GenreDto genreDto = new GenreDto();
        genreDto.setName("Fantasy");

        mockMvc.perform(post(genreControllerUrl).secure(true)
                                                .content(mapper.writeValueAsString(genreDto))
                                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("name")))));
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrow422WhenCreatingGenreWithEmptyName() throws Exception {
        GenreDto genreDto = new GenreDto();

        mockMvc.perform(post(genreControllerUrl).secure(true)
                                                .content(mapper.writeValueAsString(genreDto))
                                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("name")))));
    }

    @Test
    void shouldThrow403WhenCreatingGenreWithoutPermission() throws Exception {
        GenreDto genreDto = new GenreDto();
        genreDto.setName("Cooking");

        mockMvc.perform(post(genreControllerUrl).secure(true)
                                                .content(mapper.writeValueAsString(genreDto))
                                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldSuccessfullyUpdateGenre() throws Exception {
        GenreDto genreDto = new GenreDto();
        genreDto.setName("Cooking");

        mockMvc.perform(put(genreControllerUrl + "/1").secure(true)
                                                      .content(mapper.writeValueAsString(genreDto))
                                                      .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(1L)))
               .andExpect(jsonPath("$.name", equalTo(genreDto.getName())));
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrow422WhenUpdatingGenreWithExistingName() throws Exception {
        GenreDto genreDto = new GenreDto();
        genreDto.setName("Comedy");

        mockMvc.perform(put(genreControllerUrl + "/3").secure(true)
                                                      .content(mapper.writeValueAsString(genreDto))
                                                      .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("name")))));
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrow422WhenUpdatingGenreWithEmptyName() throws Exception {
        GenreDto genreDto = new GenreDto();

        mockMvc.perform(put(genreControllerUrl + "/1").secure(true)
                                                      .content(mapper.writeValueAsString(genreDto))
                                                      .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("name")))));
    }

    @Test
    void shouldThrow403WhenUpdatingGenreWithoutPermission() throws Exception {
        GenreDto genreDto = new GenreDto();
        genreDto.setName("Cooking");

        mockMvc.perform(put(genreControllerUrl + "/1").secure(true)
                                                      .content(mapper.writeValueAsString(genreDto))
                                                      .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrow404WhenUpdatingNonExistingGenre() throws Exception {
        GenreDto genreDto = new GenreDto();
        genreDto.setName("Cooking");

        mockMvc.perform(put(genreControllerUrl + "/999").secure(true)
                                                        .content(mapper.writeValueAsString(genreDto))
                                                        .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldSuccessfullyDeleteGenre() throws Exception {
        mockMvc.perform(delete(genreControllerUrl + "/1").secure(true))
               .andExpect(status().isNoContent());
        mockMvc.perform(get(genreControllerUrl + "/1").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldThrow403WhenDeletingGenreWithoutPermission() throws Exception {
        mockMvc.perform(delete(genreControllerUrl + "/1").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithAuthenticatedUser(roles = "ADMIN")
    void shouldThrow404WhenDeletingNonExistingGenre() throws Exception {
        mockMvc.perform(delete(genreControllerUrl + "/999").secure(true))
               .andExpect(status().isNotFound());
    }
}