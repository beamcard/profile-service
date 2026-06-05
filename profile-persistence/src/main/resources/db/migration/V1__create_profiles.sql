CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE profiles (
    id           UUID PRIMARY KEY,
    user_id      UUID        UNIQUE NOT NULL,
    username     CITEXT      UNIQUE NOT NULL,
    display_name TEXT,
    bio          TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE links (
    id         UUID PRIMARY KEY,
    profile_id UUID        NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    label      TEXT        NOT NULL,
    url        TEXT        NOT NULL,
    type       TEXT        NOT NULL DEFAULT 'GENERIC', -- GENERIC|WHATSAPP|TELEGRAM|INSTAGRAM|EMAIL|ETC
    position   INT         NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_links_profile_id ON links (profile_id);