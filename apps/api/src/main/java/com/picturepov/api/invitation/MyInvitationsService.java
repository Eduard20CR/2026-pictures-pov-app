package com.picturepov.api.invitation;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.user.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Invitations addressed to the signed-in user's verified email (RN-04). */
@Service
class MyInvitationsService {

    private final InvitationRepository invitationRepository;
    private final CurrentUserService currentUserService;

    MyInvitationsService(InvitationRepository invitationRepository, CurrentUserService currentUserService) {
        this.invitationRepository = invitationRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    List<PendingInvitationResponse> listPending(Jwt jwt) {
        return currentUserService.findVerifiedEmail(jwt)
                .map(invitationRepository::findPendingByEmail)
                .orElse(List.of());
    }

    /**
     * Checks the email live at acceptance only; accepted access is not re-checked.
     * 403 if the email is not verified or is not the invitation's; 404 if it was removed, already accepted, or its event was deleted.
     */
    @Transactional
    void accept(UUID invitationId, Jwt jwt) {
        String email = currentUserService.findVerifiedEmail(jwt)
                .orElseThrow(() -> new AccessDeniedException("Email not verified"));
        Invitation invitation = invitationRepository.findPendingById(invitationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.INVITATION_NOT_FOUND, "Invitation not found"));
        if (!invitation.getEmail().equals(email)) {
            throw new AccessDeniedException("Invitation for another email");
        }
        invitation.accept(currentUserService.findOrCreateFromToken(jwt).getId());
    }
}
