package com.picturepov.api.event;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.configuration.WebLayerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The HTTP contract only; the open/closed rules are in GuestEventServiceTest. */
@WebMvcTest(GuestEventController.class)
@Import(WebLayerTestConfiguration.class)
class GuestEventControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean GuestEventService guestEventService;

    @Test
    void guestWithoutAnAccountGets200WithTheEvent() throws Exception {
        // Arrange
        when(guestEventService.getGuestEvent("abc"))
                .thenReturn(new GuestEventResponse("abc", "Boda Ana y Luis", "#000000", LocalDate.of(2099, 12, 12), 10));

        // Act
        ResultActions result = mockMvc.perform(get("/events/{code}", "abc"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Boda Ana y Luis"))
                .andExpect(jsonPath("$.eventDate").value("2099-12-12"))
                .andExpect(jsonPath("$.imagesPerUser").value(10));
    }

    @Test
    void closedEventAnswers409WithItsCode() throws Exception {
        // Arrange
        when(guestEventService.getGuestEvent("abc"))
                .thenThrow(new ConflictException(ErrorCode.EVENT_CLOSED, "Event no longer accepts photos"));

        // Act
        ResultActions result = mockMvc.perform(get("/events/{code}", "abc"));

        // Assert
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EVENT_CLOSED"));
    }
}
