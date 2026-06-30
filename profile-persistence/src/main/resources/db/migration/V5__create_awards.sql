CREATE TABLE awards (
    id          UUID PRIMARY KEY,
    profile_id  UUID        NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    image_key   TEXT        NOT NULL,
    position    INT         NOT NULL DEFAULT 0,
    description VARCHAR(300),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_awards_profile_id ON awards (profile_id);
