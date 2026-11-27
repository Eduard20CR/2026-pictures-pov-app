package com.picturepov.api.image;

import com.picturepov.api.common.error.BadRequestException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.event.EventFixtures;
import com.picturepov.api.invitation.InvitationRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewImagesServiceTest {

    static final Instant NOON = Instant.parse("2099-01-01T12:00:00Z");

    @Mock ImageRepository imageRepository;

    UUID eventId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    @ParameterizedTest
    @EnumSource(InvitationRole.class)
    void organizerAndModeratorSeeEveryPhotoWithASignedThumbnail(InvitationRole role) {
        // Arrange
        ReviewImagesService service = serviceFor(role);
        Image photo = ImageFixtures.readyPhoto(eventId, NOON);
        when(imageRepository.findByEventIdOrderByCreatedAtDescIdDesc(eventId, Limit.of(31))).thenReturn(List.of(photo));

        // Act
        ReviewImagePage page = service.listImages(eventId, userId, 30, null);

        // Assert
        assertThat(page.items()).hasSize(1);
        ReviewImageResponse item = page.items().getFirst();
        assertThat(item.id()).isEqualTo(photo.getId());
        assertThat(item.createdAt()).isEqualTo(NOON);
        assertThat(item.thumbnailUrl())
                .contains("thumbs/" + eventId + "/" + photo.getId() + ".webp")
                .contains("X-Amz-Signature=")
                .contains("X-Amz-Expires=900");
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    void hiddenPhotosAreListedAndMarkedHidden() {
        // Arrange
        ReviewImagesService service = serviceFor(InvitationRole.OWNER);
        Image hidden = ImageFixtures.hidden(ImageFixtures.readyPhoto(eventId, NOON));
        Image visible = ImageFixtures.readyPhoto(eventId, NOON.minusSeconds(60));
        when(imageRepository.findByEventIdOrderByCreatedAtDescIdDesc(eventId, Limit.of(31))).thenReturn(List.of(hidden, visible));

        // Act
        ReviewImagePage page = service.listImages(eventId, userId, 30, null);

        // Assert
        assertThat(page.items()).extracting(ReviewImageResponse::id, ReviewImageResponse::hidden)
                .containsExactly(
                        tuple(hidden.getId(), true),
                        tuple(visible.getId(), false));
    }

    @Test
    void photoWithoutAThumbnailYetHasNoThumbnailUrl() {
        // Arrange
        ReviewImagesService service = serviceFor(InvitationRole.OWNER);
        Image processing = ImageFixtures.processingPhoto(eventId, NOON);
        when(imageRepository.findByEventIdOrderByCreatedAtDescIdDesc(eventId, Limit.of(31))).thenReturn(List.of(processing));

        // Act
        ReviewImagePage page = service.listImages(eventId, userId, 30, null);

        // Assert
        assertThat(page.items().getFirst().thumbnailUrl()).isNull();
    }

    @Test
    void morePhotosThanTheLimitGiveACursorThatContinuesAfterTheLastOne() {
        // Arrange
        ReviewImagesService service = serviceFor(InvitationRole.OWNER);
        Image first = ImageFixtures.readyPhoto(eventId, NOON);
        Image second = ImageFixtures.readyPhoto(eventId, NOON.minusSeconds(60));
        Image third = ImageFixtures.readyPhoto(eventId, NOON.minusSeconds(120));
        when(imageRepository.findByEventIdOrderByCreatedAtDescIdDesc(eventId, Limit.of(3))).thenReturn(List.of(first, second, third));
        when(imageRepository.findPageAfter(eventId, second.getCreatedAt(), second.getId(), Limit.of(3))).thenReturn(List.of(third));
        ReviewImagePage firstPage = service.listImages(eventId, userId, 2, null);

        // Act
        ReviewImagePage nextPage = service.listImages(eventId, userId, 2, firstPage.nextCursor());

        // Assert
        assertThat(firstPage.items()).extracting(ReviewImageResponse::id).containsExactly(first.getId(), second.getId());
        assertThat(nextPage.items()).extracting(ReviewImageResponse::id).containsExactly(third.getId());
        assertThat(nextPage.nextCursor()).isNull();
    }

    @Test
    void exactlyLimitPhotosIsTheLastPage() {
        // Arrange
        ReviewImagesService service = serviceFor(InvitationRole.OWNER);
        List<Image> photos = List.of(ImageFixtures.readyPhoto(eventId, NOON), ImageFixtures.readyPhoto(eventId, NOON.minusSeconds(60)));
        when(imageRepository.findByEventIdOrderByCreatedAtDescIdDesc(eventId, Limit.of(3))).thenReturn(photos);

        // Act
        ReviewImagePage page = service.listImages(eventId, userId, 2, null);

        // Assert
        assertThat(page.items()).hasSize(2);
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    void malformedCursorGets400() {
        // Arrange
        ReviewImagesService service = serviceFor(InvitationRole.OWNER);

        // Act
        Throwable thrown = catchThrowable(() -> service.listImages(eventId, userId, 30, "not-a-cursor"));

        // Assert
        assertThat(thrown).isInstanceOf(BadRequestException.class)
                .extracting("code").isEqualTo(ErrorCode.INVALID_CURSOR);
    }

    @Test
    void userWithoutAnAcceptedInvitationGets404() {
        // Arrange
        ReviewImagesService service = serviceFor(InvitationRole.OWNER);

        // Act
        Throwable thrown = catchThrowable(() -> service.listImages(eventId, UUID.randomUUID(), 30, null));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    /** userId holds the role on eventId; anyone else has no access. */
    private ReviewImagesService serviceFor(InvitationRole role) {
        return new ReviewImagesService(EventFixtures.eventAccess(eventId, userId, role), imageRepository,
                ImageFixtures.photoLinkSigner(), Duration.ofMinutes(15));
    }
}
