package com.picturepov.api.image;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.event.EventFixtures;
import com.picturepov.api.invitation.InvitationRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageDownloadServiceTest {

    @Mock ImageRepository imageRepository;

    UUID eventId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    @Test
    void organizerGetsASignedLinkThatSavesTheOriginalWithItsExtension() {
        // Arrange
        ImageDownloadService service = serviceFor(InvitationRole.OWNER);
        Image photo = ImageFixtures.hidden(ImageFixtures.readyPhoto(eventId, Instant.now()));
        when(imageRepository.findByIdAndEventIdAndStatus(photo.getId(), eventId, ImageStatus.READY)).thenReturn(Optional.of(photo));

        // Act
        ImageDownloadResponse response = service.downloadLink(eventId, photo.getId(), userId);

        // Assert
        assertThat(response.url())
                .contains("originals/" + eventId + "/" + photo.getId())
                .contains("X-Amz-Signature=")
                .contains("X-Amz-Expires=900")
                .contains("response-content-disposition=" + encode("attachment; filename=\"" + photo.getId() + ".jpg\""));
    }

    @Test
    void photoThatIsMissingFromAnotherEventOrStillProcessingGets404() {
        // Arrange
        ImageDownloadService service = serviceFor(InvitationRole.OWNER);
        UUID imageId = UUID.randomUUID();
        when(imageRepository.findByIdAndEventIdAndStatus(imageId, eventId, ImageStatus.READY)).thenReturn(Optional.empty());

        // Act
        Throwable thrown = catchThrowable(() -> service.downloadLink(eventId, imageId, userId));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.IMAGE_NOT_FOUND);
    }

    @Test
    void moderatorCannotDownload() {
        // Arrange
        ImageDownloadService service = serviceFor(InvitationRole.MODERATOR);

        // Act
        Throwable thrown = catchThrowable(() -> service.downloadLink(eventId, UUID.randomUUID(), userId));

        // Assert
        assertThat(thrown).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(imageRepository);
    }

    @Test
    void userWithoutAnAcceptedInvitationGets404() {
        // Arrange
        ImageDownloadService service = serviceFor(InvitationRole.OWNER);

        // Act
        Throwable thrown = catchThrowable(() -> service.downloadLink(eventId, UUID.randomUUID(), UUID.randomUUID()));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
        verifyNoInteractions(imageRepository);
    }

    /** userId holds the role on eventId; anyone else has no access. */
    private ImageDownloadService serviceFor(InvitationRole role) {
        return new ImageDownloadService(EventFixtures.eventAccess(eventId, userId, role), imageRepository,
                ImageFixtures.photoLinkSigner(), Duration.ofMinutes(15));
    }

    /** As the AWS signer encodes query values. */
    private static String encode(String value) {
        return URLEncoder.encode(value, UTF_8).replace("+", "%20");
    }
}
