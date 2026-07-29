CREATE TABLE opening_hours (
    id             UUID PRIMARY KEY,
    affiliation_id UUID        NOT NULL REFERENCES affiliations(id) ON DELETE CASCADE,
    day_of_week    VARCHAR(9)  NOT NULL,
    open_time      VARCHAR(5)  NOT NULL,
    close_time     VARCHAR(5)  NOT NULL,
    position       INT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_opening_hours_affiliation_id ON opening_hours (affiliation_id);
