package com.picturepov.api.zipexport;

import com.picturepov.api.configuration.WebLayerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The HTTP contract only; the ZIP rules are in ZipExportServiceTest. */
@WebMvcTest(ZipExportController.class)
@Import(WebLayerTestConfiguration.class)
class ZipExportControllerTest {

    static final Instant REQUESTED_AT = Instant.parse("2099-01-01T10:00:00Z");

    @Autowired MockMvc mockMvc;
    @MockitoBean ZipExportService zipExportService;

    UUID eventId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    @Test
    void requestingTheZipAnswers202ForTheSignedInUser() throws Exception {
        // Arrange
        when(zipExportService.request(eventId, userId)).thenReturn(
                new ZipExportResponse(ZipExportResponse.State.IN_PROGRESS, REQUESTED_AT, null, null, null));

        // Act
        ResultActions result = mockMvc.perform(post("/me/events/{id}/zip-export", eventId).with(signedIn()));

        // Assert
        result.andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.requestedAt").value("2099-01-01T10:00:00Z"));
    }

    @Test
    void latestZipAnswers200WithItsLink() throws Exception {
        // Arrange
        when(zipExportService.latest(eventId, userId)).thenReturn(Optional.of(
                new ZipExportResponse(ZipExportResponse.State.AVAILABLE, REQUESTED_AT, 12, 3_400_000L, "https://signed")));

        // Act
        ResultActions result = mockMvc.perform(get("/me/events/{id}/zip-export", eventId).with(signedIn()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.imageCount").value(12))
                .andExpect(jsonPath("$.sizeBytes").value(3_400_000))
                .andExpect(jsonPath("$.downloadUrl").value("https://signed"));
    }

    @Test
    void eventWithoutAZipAnswers204() throws Exception {
        // Arrange
        when(zipExportService.latest(eventId, userId)).thenReturn(Optional.empty());

        // Act
        ResultActions result = mockMvc.perform(get("/me/events/{id}/zip-export", eventId).with(signedIn()));

        // Assert
        result.andExpect(status().isNoContent());
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        // Act
        ResultActions onRequest = mockMvc.perform(post("/me/events/{id}/zip-export", eventId));
        ResultActions onLatest = mockMvc.perform(get("/me/events/{id}/zip-export", eventId));

        // Assert
        onRequest.andExpect(status().isUnauthorized());
        onLatest.andExpect(status().isUnauthorized());
        verifyNoInteractions(zipExportService);
    }

    private RequestPostProcessor signedIn() {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }
}
