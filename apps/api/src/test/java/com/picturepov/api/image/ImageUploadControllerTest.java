package com.picturepov.api.image;

import com.picturepov.api.configuration.WebLayerTestConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The HTTP contract only; the upload rules are in ImageUploadServiceTest. */
@WebMvcTest(ImageUploadController.class)
@Import(WebLayerTestConfiguration.class)
class ImageUploadControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ImageUploadService imageUploadService;

    @Test
    void guestOfTheCookieGets201WithTheUploadUrl() throws Exception {
        // Arrange
        UUID guestId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        when(imageUploadService.createPresignedUpload("abc", guestId, new PresignedUploadRequest("image/jpeg", 1000)))
                .thenReturn(new PresignedUploadResponse(imageId, "https://signed", Instant.parse("2099-01-01T10:05:00Z")));

        // Act
        ResultActions result = mockMvc.perform(post("/events/{code}/uploads", "abc").servletPath("/events/abc/uploads")
                .cookie(new Cookie("guest_id", guestId.toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"contentType\": \"image/jpeg\", \"sizeBytes\": 1000 }"));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageId").value(imageId.toString()))
                .andExpect(jsonPath("$.uploadUrl").value("https://signed"))
                .andExpect(jsonPath("$.expiresAt").value("2099-01-01T10:05:00Z"));
    }

    @Test
    void malformedBodyAnswers400() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(post("/events/{code}/uploads", "abc").servletPath("/events/abc/uploads")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"contentType\": "));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST_BODY"));
        verifyNoInteractions(imageUploadService);
    }
}
