package com.picturepov.api.image;

import com.picturepov.api.configuration.WebLayerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The HTTP contract only; the review and paging rules are in ReviewImagesServiceTest. */
@WebMvcTest(ReviewImagesController.class)
@Import(WebLayerTestConfiguration.class)
class ReviewImagesControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ReviewImagesService reviewImagesService;

    UUID eventId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    @Test
    void firstPageAsksForThirtyPhotosAndAnswers200() throws Exception {
        // Arrange
        UUID photo = UUID.randomUUID();
        when(reviewImagesService.listImages(eventId, userId, 30, null)).thenReturn(new ReviewImagePage(
                List.of(new ReviewImageResponse(photo, "https://thumb", true, Instant.parse("2099-01-01T10:00:00Z"))), "next"));

        // Act
        ResultActions result = mockMvc.perform(get("/me/events/{id}/images", eventId).with(signedIn()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(photo.toString()))
                .andExpect(jsonPath("$.items[0].thumbnailUrl").value("https://thumb"))
                .andExpect(jsonPath("$.items[0].hidden").value(true))
                .andExpect(jsonPath("$.items[0].createdAt").value("2099-01-01T10:00:00Z"))
                .andExpect(jsonPath("$.nextCursor").value("next"));
    }

    @Test
    void limitAndCursorArePassedOn() throws Exception {
        // Arrange
        when(reviewImagesService.listImages(eventId, userId, 5, "abc")).thenReturn(new ReviewImagePage(List.of(), null));

        // Act
        ResultActions result = mockMvc.perform(get("/me/events/{id}/images", eventId).with(signedIn())
                .param("limit", "5").param("cursor", "abc"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "101"})
    void limitOutsideOneToHundredAnswers400(String limit) throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/me/events/{id}/images", eventId).with(signedIn()).param("limit", limit));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        verifyNoInteractions(reviewImagesService);
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/me/events/{id}/images", eventId));

        // Assert
        result.andExpect(status().isUnauthorized());
        verifyNoInteractions(reviewImagesService);
    }

    private RequestPostProcessor signedIn() {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }
}
