https://mermaid.ai/live

erDiagram

USERS |o..o{ INVITATIONS : "accepts"
EVENTS ||..o{ INVITATIONS : "has"
EVENTS ||..o{ IMAGES : "contains"
EVENTS ||..o{ ZIP_EXPORTS : "produces"
EVENTS ||..o{ GUEST_LIMITS : "tracks uploads of"
USERS ||..o{ ZIP_EXPORTS : "requests"

    USERS {
        uuid id PK "sub de Cognito"
        varchar(320) email UK
        varchar(255) name "nullable"
        timestamptz created_at
    }

    INVITATIONS {
        uuid id PK
        uuid event_id FK "UNIQUE(event_id, email)"
        varchar(320) email "UNIQUE(event_id, email), lowercase"
        uuid user_id FK "nullable, null until accepted"
        varchar(20) role "CHECK OWNER|MODERATOR, one OWNER per event"
        varchar(20) status "CHECK PENDING|ACCEPTED"
        timestamptz created_at
    }

    EVENTS {
        uuid id PK
        varchar(100) name
        varchar(7) color "#RRGGBB"
        varchar(16) public_code UK
        date event_date
        int images_per_user
        int expected_guests "nullable"
        varchar(20) status "CHECK DRAFT|ACTIVE|CLOSED|ARCHIVED|DELETED"
        timestamptz created_at
    }

    IMAGES {
        uuid id PK
        uuid event_id FK
        varchar(1024) s3_key
        varchar(1024) thumb_key "nullable"
        varchar(100) content_type
        bigint size_bytes
        uuid guest_id
        varchar(20) status "CHECK PENDING|READY|HIDDEN"
        timestamptz created_at
    }

    ZIP_EXPORTS {
        uuid id PK
        uuid event_id FK
        uuid requested_by FK
        varchar(1024) s3_key "nullable"
        varchar(20) status "CHECK PENDING|RUNNING|READY|FAILED"
        int image_count
        bigint size_bytes
        varchar(1000) error_message "nullable"
        timestamptz created_at
        timestamptz completed_at "nullable"
        timestamptz expires_at "nullable"
    }

    GUEST_LIMITS {
        uuid id PK
        uuid event_id FK "UNIQUE(event_id, guest_id)"
        uuid guest_id "UNIQUE(event_id, guest_id)"
        int upload_count
        timestamptz created_at
        timestamptz updated_at
    }