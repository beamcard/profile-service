CREATE TABLE profile_locations (
    profile_id UUID PRIMARY KEY REFERENCES profiles(id) ON DELETE CASCADE,
    country    VARCHAR(60),
    city       VARCHAR(85)
);
