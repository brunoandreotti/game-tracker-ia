-- Clamp legacy 1–10 ratings into 0–5, then enforce the range.
UPDATE tracked_game
SET rating = 5
WHERE rating > 5;

UPDATE tracked_game
SET rating = 0
WHERE rating < 0;

ALTER TABLE tracked_game
    ADD CONSTRAINT chk_tracked_game_rating_range
        CHECK (rating IS NULL OR (rating >= 0 AND rating <= 5));
