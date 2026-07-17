CREATE TABLE price_items (
    id         UUID PRIMARY KEY,
    profile_id UUID          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    name       VARCHAR(500)  NOT NULL,
    price_type VARCHAR(16)   NOT NULL,
    amount_min NUMERIC(12, 2),
    amount_max NUMERIC(12, 2),
    position   INT           NOT NULL DEFAULT 0
);

CREATE INDEX idx_price_items_profile_id ON price_items (profile_id);
