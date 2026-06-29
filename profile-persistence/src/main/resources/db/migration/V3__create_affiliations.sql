CREATE TABLE affiliations (
    id           UUID PRIMARY KEY,
    profile_id   UUID         NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    role         VARCHAR(80),
    organization VARCHAR(120),
    address      VARCHAR(200),
    description  VARCHAR(300),
    position     INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_affiliations_profile_id ON affiliations (profile_id);
