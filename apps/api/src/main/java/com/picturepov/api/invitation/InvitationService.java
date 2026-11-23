package com.picturepov.api.invitation;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;

    InvitationService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    public Invitation createOwnerInvitation(UUID eventId, String email) {
        return invitationRepository.save(Invitation.createOwner(eventId, email));
    }
}