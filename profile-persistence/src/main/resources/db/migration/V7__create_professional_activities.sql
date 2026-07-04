CREATE TABLE professional_activities (
    id         UUID PRIMARY KEY,
    profile_id UUID         NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    name       VARCHAR(200) NOT NULL,
    position   INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_professional_activities_profile_id ON professional_activities (profile_id);
