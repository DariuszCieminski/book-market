package pl.bookmarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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
import org.thymeleaf.util.StringUtils;
import pl.bookmarket.dto.MessageCreateDto;
import pl.bookmarket.testhelpers.datafactory.MessageBuilder;
import pl.bookmarket.testhelpers.datafactory.UserBuilder;
import pl.bookmarket.testhelpers.utils.WithAuthenticatedUser;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Value("${bm.controllers.message}")
    private String messageControllerUrl;

    @Value("${bm.controllers.user}")
    private String userControllerUrl;

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = "?filter=ALL")
    void shouldSuccessfullyReturnAllMessagesForGivenUser(String filter) throws Exception {
        mockMvc.perform(get(userControllerUrl + "/1/messages" + filter).secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void shouldSuccessfullyReturnReceivedMessagesForGivenUser() throws Exception {
        mockMvc.perform(get(userControllerUrl + "/1/messages?filter=received").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].id", equalsId(2L)))
               .andExpect(jsonPath("$[0].text", equalTo("Thanks, I am fine :)")));
    }

    @Test
    @WithAuthenticatedUser(id = 2)
    void shouldSuccessfullyReturnUnreadMessagesForGivenUser() throws Exception {
        mockMvc.perform(get(userControllerUrl + "/2/messages?filter=unread").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].id", equalsId(3L)))
               .andExpect(jsonPath("$[0].text", equalTo("Please check my books for sale.")))
               .andExpect(jsonPath("$[0].read", is(false)));
    }

    @Test
    @WithAuthenticatedUser(id = 2, roles = "ADMIN")
    void shouldThrow403WhenGettingMessagesForAnotherUserAsAdmin() throws Exception {
        mockMvc.perform(get(userControllerUrl + "/1/messages").secure(true))
               .andExpect(status().isForbidden());
    }

    @Test
    @Sql(value = "/deleteMessages.sql", executionPhase = BEFORE_TEST_METHOD)
    @SqlMergeMode(MERGE)
    void shouldSuccessfullyCreateMessage() throws Exception {
        MessageCreateDto messageCreateDto = MessageBuilder.getDefaultMessage()
                                                          .withText(StringUtils.repeat("x", 300))
                                                          .buildMessageCreateDto();

        mockMvc.perform(post(messageControllerUrl).secure(true)
                                                  .content(mapper.writeValueAsString(messageCreateDto))
                                                  .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", notNullValue()))
               .andExpect(jsonPath("$.text", equalTo(messageCreateDto.getText())))
               .andExpect(jsonPath("$.text", equalTo(messageCreateDto.getText())))
               .andExpect(jsonPath("$.sender.id", equalsId(1L)))
               .andExpect(jsonPath("$.receiver.id", equalsId(2L)))
               .andExpect(jsonPath("$.read", is(false)))
               .andExpect(jsonPath("$.sendTime", notNullValue()));
    }

    @Test
    void shouldThrow422WhenCreatingMessageWithNullReceiver() throws Exception {
        MessageCreateDto messageCreateDto = MessageBuilder.getDefaultMessage()
                                                          .withReceiver(null)
                                                          .buildMessageCreateDto();

        mockMvc.perform(post(messageControllerUrl).secure(true)
                                                  .content(mapper.writeValueAsString(messageCreateDto))
                                                  .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("receiver")))));
    }

    @Test
    void shouldThrow422WhenCreatingMessageWithNonExistentReceiver() throws Exception {
        MessageCreateDto messageCreateDto = MessageBuilder.getDefaultMessage()
                                                          .withReceiver(UserBuilder.getDefaultUser().withId(999L)
                                                                                   .build())
                                                          .buildMessageCreateDto();

        mockMvc.perform(post(messageControllerUrl).secure(true)
                                                  .content(mapper.writeValueAsString(messageCreateDto))
                                                  .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("receiver")))));
    }

    @Test
    void shouldThrow422WhenCreatingMessageWithCurrentUserAsReceiver() throws Exception {
        MessageCreateDto messageCreateDto = MessageBuilder.getDefaultMessage()
                                                          .withReceiver(UserBuilder.getDefaultUser().withId(1L).build())
                                                          .buildMessageCreateDto();

        mockMvc.perform(post(messageControllerUrl).secure(true)
                                                  .content(mapper.writeValueAsString(messageCreateDto))
                                                  .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("receiver")))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("getInvalidMessageText")
    void shouldThrow422WhenCreatingMessageWithInvalidText(String text) throws Exception {
        MessageCreateDto messageCreateDto = MessageBuilder.getDefaultMessage().withText(text).buildMessageCreateDto();

        mockMvc.perform(post(messageControllerUrl).secure(true)
                                                  .content(mapper.writeValueAsString(messageCreateDto))
                                                  .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("text")))));
    }

    @Test
    void shouldSuccessfullyUpdateMessage() throws Exception {
        MessageCreateDto messageDto = MessageBuilder.getDefaultMessage()
                                                    .withId(1L)
                                                    .withText("This is an updated message")
                                                    .withRead(false)
                                                    .withReceiver(UserBuilder.getDefaultUser().withId(1L).build())
                                                    .buildMessageCreateDto();

        mockMvc.perform(put(messageControllerUrl + "/1").secure(true)
                                                        .content(mapper.writeValueAsString(messageDto))
                                                        .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalsId(1L)))
               .andExpect(jsonPath("$.text", equalTo(messageDto.getText())))
               .andExpect(jsonPath("$.sender.id", equalsId(1L)))
               .andExpect(jsonPath("$.receiver.id", equalsId(2L)))
               .andExpect(jsonPath("$.read", is(false)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("getInvalidMessageText")
    void shouldThrow422WhenUpdatingMessageWithInvalidText(String text) throws Exception {
        MessageCreateDto messageCreateDto = MessageBuilder.getDefaultMessage()
                                                          .withId(1L)
                                                          .withText(text)
                                                          .buildMessageCreateDto();

        mockMvc.perform(put(messageControllerUrl + "/1").secure(true)
                                                        .content(mapper.writeValueAsString(messageCreateDto))
                                                        .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors", hasItem(hasEntry(equalTo("field"), equalTo("text")))));
    }

    @Test
    void shouldThrow404WhenUpdatingNonExistentMessage() throws Exception {
        MessageCreateDto messageCreateDto = MessageBuilder.getDefaultMessage().buildMessageCreateDto();

        mockMvc.perform(put(messageControllerUrl + "/999").secure(true)
                                                          .content(mapper.writeValueAsString(messageCreateDto))
                                                          .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldThrow403WhenUpdatingMessageOfAnotherUser() throws Exception {
        MessageCreateDto messageCreateDto = MessageBuilder.getDefaultMessage().buildMessageCreateDto();

        mockMvc.perform(put(messageControllerUrl + "/2").secure(true)
                                                        .content(mapper.writeValueAsString(messageCreateDto))
                                                        .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isForbidden());
    }

    @Test
    @Sql(statements = "UPDATE MESSAGE SET IS_READ = FALSE;", executionPhase = BEFORE_TEST_METHOD)
    @SqlMergeMode(MERGE)
    void shouldSuccessfullySetMessagesWithCurrentUserBeingReceiverAsRead() throws Exception {
        List<Long> messageIds = Arrays.asList(1L, 2L, 3L);

        mockMvc.perform(patch(messageControllerUrl).secure(true)
                                                   .content(mapper.writeValueAsString(messageIds))
                                                   .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNoContent());

        mockMvc.perform(get(userControllerUrl + "/1/messages").secure(true))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[*].id", contains(equalsId(1L), equalsId(2L), equalsId(3L))))
               .andExpect(jsonPath("$[*].read", contains(is(false), is(true), is(false))));
    }

    @Test
    void shouldSuccessfullyDeleteMessage() throws Exception {
        mockMvc.perform(delete(messageControllerUrl + "/1").secure(true))
               .andExpect(status().isNoContent());
    }

    @Test
    void shouldThrow404WhenDeletingNonExistentMessage() throws Exception {
        mockMvc.perform(delete(messageControllerUrl + "/999").secure(true))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldThrow403WhenDeletingMessageSentByAnotherUser() throws Exception {
        mockMvc.perform(delete(messageControllerUrl + "/2").secure(true))
               .andExpect(status().isForbidden());
    }

    private static List<String> getInvalidMessageText() {
        return Arrays.asList(StringUtils.repeat("a", 301));
    }
}