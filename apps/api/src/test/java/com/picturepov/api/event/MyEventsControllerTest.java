package com.picturepov.api.event;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.configuration.WebLayerTestConfiguration;
import com.picturepov.api.invitation.InvitationRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The HTTP contract only; the access rules are in MyEventsServiceTest and EventAccessTest. */
@WebMvcTest(MyEventsController.class)
@Import(WebLayerTestConfiguration.class)
class MyEventsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean MyEventsService myEventsService;

    UUID userId = UUID.randomUUID();
    MyEventResponse event = new MyEventResponse(UUID.randomUUID(), "bodaAnaYLuis0000000000", "Boda Ana y Luis",
            LocalDate.of(2099, 12, 12), EventStatus.OPEN, InvitationRole.OWNER);

    @Test
    void listingAnswers200WithTheSignedInUsersEvents() throws Exception {
        // Arrange
        when(myEventsService.listMyEvents(userId)).thenReturn(List.of(event));

        // Act
        ResultActions result = mockMvc.perform(get("/me/events").with(signedIn()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(event.id().toString()))
                .andExpect(jsonPath("$[0].eventDate").value("2099-12-12"))
                .andExpect(jsonPath("$[0].role").value("OWNER"));
    }

    @Test
    void openingAnEventAnswers200WithIt() throws Exception {
        // Arrange
        when(myEventsService.getMyEvent(event.id(), userId)).thenReturn(event);

        // Act
        ResultActions result = mockMvc.perform(get("/me/events/{id}", event.id()).with(signedIn()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Boda Ana y Luis"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    void eventOutOfReachAnswers404WithItsCode() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        when(myEventsService.getMyEvent(eventId, userId))
                .thenThrow(new NotFoundException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));

        // Act
        ResultActions result = mockMvc.perform(get("/me/events/{id}", eventId).with(signedIn()));

        // Assert
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        // Act
        ResultActions list = mockMvc.perform(get("/me/events"));
        ResultActions open = mockMvc.perform(get("/me/events/{id}", UUID.randomUUID()));

        // Assert
        list.andExpect(status().isUnauthorized());
        open.andExpect(status().isUnauthorized());
        verifyNoInteractions(myEventsService);
    }

    private RequestPostProcessor signedIn() {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }
}
