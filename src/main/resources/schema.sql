CREATE TABLE IF NOT EXISTS blueprints (
    author VARCHAR(100) NOT NULL,
    name   VARCHAR(100) NOT NULL,
    PRIMARY KEY (author, name)
);

CREATE TABLE IF NOT EXISTS blueprint_points (
    author VARCHAR(100) NOT NULL,
    name   VARCHAR(100) NOT NULL,
    seq    INTEGER NOT NULL,
    x      INTEGER NOT NULL,
    y      INTEGER NOT NULL,
    PRIMARY KEY (author, name, seq),
    FOREIGN KEY (author, name) REFERENCES blueprints (author, name) ON DELETE CASCADE
);
