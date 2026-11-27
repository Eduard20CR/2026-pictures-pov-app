package com.picturepov.api.invitation;

/** A real InvitationService for unit tests in other packages; only its database is replaced. */
public final class InvitationFixtures {

    private InvitationFixtures() {}

    /** For createOwnerInvitation, which checks no access. */
    public static InvitationService invitationService(InvitationRepository invitationRepository) {
        return new InvitationService(invitationRepository, null);
    }
}
