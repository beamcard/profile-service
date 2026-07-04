CREATE TABLE showcases (
    id         UUID PRIMARY KEY,
    profile_id UUID         NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title      VARCHAR(120),
    intro      VARCHAR(500),
    position   INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_showcases_profile_id ON showcases (profile_id);

CREATE TABLE showcase_steps (
    id          UUID PRIMARY KEY,
    showcase_id UUID         NOT NULL REFERENCES showcases(id) ON DELETE CASCADE,
    image_key   TEXT         NOT NULL,
    description VARCHAR(300),
    position    INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_showcase_steps_showcase_id ON showcase_steps (showcase_id);
