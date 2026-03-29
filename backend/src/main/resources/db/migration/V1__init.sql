CREATE TABLE seasons (
    id BIGSERIAL PRIMARY KEY,
    started_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE players (
    id UUID PRIMARY KEY,
    nickname VARCHAR(64) NOT NULL,
    energy INT NOT NULL DEFAULT 20,
    last_energy_update TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    score INT NOT NULL DEFAULT 0,
    last_action_at TIMESTAMPTZ,
    actions_since_captcha INT NOT NULL DEFAULT 0,
    captcha_passed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE cells (
    id BIGSERIAL PRIMARY KEY,
    x INT NOT NULL,
    y INT NOT NULL,
    owner_player_id UUID REFERENCES players (id) ON DELETE SET NULL,
    owner_nickname VARCHAR(64),
    CONSTRAINT cells_xy_unique UNIQUE (x, y)
);

CREATE TABLE actions (
    id BIGSERIAL PRIMARY KEY,
    player_id UUID NOT NULL REFERENCES players (id) ON DELETE CASCADE,
    cell_id BIGINT NOT NULL REFERENCES cells (id) ON DELETE CASCADE,
    action_type VARCHAR(16) NOT NULL,
    success BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_actions_player ON actions (player_id);
CREATE INDEX idx_actions_created ON actions (created_at);

CREATE TABLE season_leaderboard_results (
    id BIGSERIAL PRIMARY KEY,
    player_id UUID NOT NULL,
    player_nickname VARCHAR(64) NOT NULL,
    score INT NOT NULL,
    rank INT NOT NULL,
    season_start TIMESTAMPTZ NOT NULL,
    season_end TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_season_results_season ON season_leaderboard_results (season_start, season_end);

CREATE TABLE overall_leaderboard_results (
    id BIGSERIAL PRIMARY KEY,
    player_id UUID NOT NULL,
    player_nickname VARCHAR(64) NOT NULL,
    score INT NOT NULL,
    rank INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
