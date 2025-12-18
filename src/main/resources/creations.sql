use movie_schema;
-- Drop table if exists (for clean re-initialization)
DROP TABLE IF EXISTS movies;

-- ============================================
-- Movies Table
-- ============================================
CREATE TABLE movies (
                        id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
                        title           VARCHAR(255)    NOT NULL,
                        director        VARCHAR(255),
                        release_year    INTEGER,
                        rating          DECIMAL(3,1),
                        rating_status   VARCHAR(20)     DEFAULT 'PENDING',
                        created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
                        updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                        CONSTRAINT chk_release_year CHECK (release_year IS NULL OR (release_year >= 1888 AND release_year <= 2100)),
                        CONSTRAINT chk_rating CHECK (rating IS NULL OR (rating >= 0 AND rating <= 10)),
                        CONSTRAINT chk_rating_status CHECK (rating_status IN ('PENDING', 'ENRICHED', 'NOT_FOUND', 'ERROR'))
);

-- ============================================
-- Indexes for Performance
-- ============================================
CREATE INDEX idx_movies_title ON movies(title);
CREATE INDEX idx_movies_director ON movies(director);
CREATE INDEX idx_movies_release_year ON movies(release_year);
CREATE INDEX idx_movies_rating_status ON movies(rating_status);