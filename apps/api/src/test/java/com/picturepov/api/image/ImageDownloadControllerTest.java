package com.picturepov.api.image;

import com.picturepov.api.configuration.WebLayerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The HTTP contract only; the download rules are in ImageDownloadServiceTest. */
@WebMvcTest(ImageDownloadController.class)
@Import(WebLayerTestConfiguration.class)
class ImageDownloadControllerTest {

    static final String PATH = "/me/events/{id}/images/{imageId}/download";

    @Autowired MockMvc mockMvc;
    @MockitoBean ImageDownloadService imageDownloadService;

    UUID eventId = UUID.randomUUID();
    UUID imageId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    @Test
    void organizerGets200WithTheSignedLink() throws Exception {
        // Arrange
        when(imageDownloadService.downloadLink(eventId, imageId, userId)).thenReturn(new ImageDownloadResponse("https://signed"));

        // Act
        ResultActions result = mockMvc.perform(get(PATH, eventId, imageId).with(signedIn()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://signed"));
    }

    @Test
    void refusedActionAnswers403WithItsCode() throws Exception {
        // Arrange
        when(imageDownloadService.downloadLink(eventId, imageId, userId)).thenThrow(new AccessDeniedException("Organizer only"));

        // Act
        ResultActions result = mockMvc.perform(get(PATH, eventId, imageId).with(signedIn()));

        // Assert
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get(PATH, eventId, imageId));

        // Assert
        result.andExpect(status().isUnauthorized());
        verifyNoInteractions(imageDownloadService);
    }

    private RequestPostProcessor signedIn() {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }
}
