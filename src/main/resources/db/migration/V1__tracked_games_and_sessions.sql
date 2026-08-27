CREATE TABLE tracked_game (
    id BIGSERIAL PRIMARY KEY,
    rawg_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    year INTEGER,
    cover_url VARCHAR(2048),
    status VARCHAR(32) NOT NULL,
    rating INTEGER,
    CONSTRAINT uq_tracked_game_rawg_id UNIQUE (rawg_id)
);

CREATE TABLE play_session (
    id BIGSERIAL PRIMARY KEY,
    tracked_game_id BIGINT NOT NULL REFERENCES tracked_game (id) ON DELETE CASCADE,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    played_at DATE NOT NULL
);

CREATE INDEX idx_play_session_tracked_game_id ON play_session (tracked_game_id);
CREATE INDEX idx_play_session_played_at ON play_session (tracked_game_id, played_at DESC);
