package com.picturepov.api.invitation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
}