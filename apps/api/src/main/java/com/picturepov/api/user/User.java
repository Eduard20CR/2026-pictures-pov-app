package com.picturepov.api.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(name = "email")
    private String email;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected User() {}

    public User(UUID id, String email) {
        this.id = id;
        this.email = email;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
}
