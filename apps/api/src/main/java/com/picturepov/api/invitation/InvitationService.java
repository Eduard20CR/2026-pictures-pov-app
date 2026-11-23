package com.picturepov.api.invitation;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.event.EventAccess;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final EventAccess eventAccess;

    InvitationService(InvitationRepository invitationRepository, EventAccess eventAccess) {
        this.invitationRepository = invitationRepository;
        this.eventAccess = eventAccess;
    }

    public Invitation createOwnerInvitation(UUID eventId, String email) {
        return invitationRepository.save(Invitation.createOwner(eventId, email));
    }

    /** Organizer only (RN-05); the invitee needs no account yet (RF-ORG-22). One invitation per event and email. */
    @Transactional
    InvitationResponse inviteModerator(UUID eventId, UUID userId, String email) {
        eventAccess.requireOrganizer(eventId, userId);
        if (invitationRepository.existsByEventIdAndEmail(eventId, Invitation.normalizeEmail(email))) {
            throw invitationAlreadyExists();
        }
        try {
            // Flushed here so a concurrent duplicate hits uq_invitations_event_email now and gets 409, not 500.
            return InvitationResponse.from(invitationRepository.saveAndFlush(Invitation.createModerator(eventId, email)));
        } catch (DataIntegrityViolationException e) {
            throw invitationAlreadyExists();
        }
    }

    private static ConflictException invitationAlreadyExists() {
        return new ConflictException(ErrorCode.INVITATION_ALREADY_EXISTS, "The email already has an invitation to the event");
    }
}
