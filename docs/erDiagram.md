erDiagram
    
USER |o..o{ EVENT : "owns"
EVENT ||..o{ IMAGE : "contains"
EVENT ||..o{ ZIP_EXPORT : "produces"
EVENT ||..o{ GUEST_LIMIT : "tracks uploads of"
USER ||..o{ ZIP_EXPORT : "requests"

    USER {
        uuid id PK "sub de Cognito"
        string email UK
        string name
        timestamp created_at
    }

    EVENT {
        uuid id PK
        uuid owner_id FK "nullable"
        string name
        string color
        string public_code UK
        date event_date
        int images_per_user
        int expected_guests "nullable"
        string status
        timestamp created_at
    }

    IMAGE {
        uuid id PK
        uuid event_id FK
        string s3_key
        string thumb_key "nullable"
        string content_type
        bigint size_bytes
        uuid guest_id
        timestamp created_at
        string status
    }

    ZIP_EXPORT{
        uuid id PK
        uuid event_id FK
        uuid requested_by FK
        string s3_key "nullable"
        string status
        int image_count
        bigint size_bytes
        string error_message "nullable"
        timestamp created_at
        timestamp completed_at "nullable"
        timestamp expires_at "nullable"
    }

    GUEST_LIMIT {
        uuid id PK
        uuid event_id FK, UK "UNIQUE(event_id, guest_id)"
        uuid guest_id UK "UNIQUE(event_id, guest_id)"
        int upload_count
        timestamp created_at
        timestamp updated_at
    }