-- =========================================================
-- 4th-study database schema (MariaDB)
-- =========================================================

CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    nickname   VARCHAR(50)  NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS accounts
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    name       VARCHAR(100) NOT NULL,
    balance    BIGINT       NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    CONSTRAINT fk_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categories
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    name       VARCHAR(50) NOT NULL,
    type       VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_categories_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_categories_user_name_type
        UNIQUE (user_id, name, type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS transactions
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    category_id      BIGINT       NOT NULL,
    type             VARCHAR(20)  NOT NULL,
    amount           BIGINT       NOT NULL,
    description      VARCHAR(255) NULL,
    transaction_date DATE         NOT NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT chk_transactions_amount
        CHECK (amount >= 1)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS refresh_tokens
(
    user_email  VARCHAR(100) PRIMARY KEY,
    token       VARCHAR(512) NOT NULL,
    expiry_date DATETIME(6)  NOT NULL,
    CONSTRAINT fk_refresh_tokens_user_email
        FOREIGN KEY (user_email) REFERENCES users (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS default_categories
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT IGNORE INTO default_categories (name, type) VALUES ('식비 (Food)', 'EXPENSE');
INSERT IGNORE INTO default_categories (name, type) VALUES ('교통비 (Traffic)', 'EXPENSE');
INSERT IGNORE INTO default_categories (name, type) VALUES ('쇼핑 (Shopping)', 'EXPENSE');
INSERT IGNORE INTO default_categories (name, type) VALUES ('기타 (Etc)', 'EXPENSE');
INSERT IGNORE INTO default_categories (name, type) VALUES ('급여 (Salary)', 'INCOME');
INSERT IGNORE INTO default_categories (name, type) VALUES ('보너스 (Bonus)', 'INCOME');
INSERT IGNORE INTO default_categories (name, type) VALUES ('기타 (Etc)', 'INCOME');

-- =========================================================
-- Indexes
-- =========================================================

CREATE INDEX idx_accounts_user_id ON accounts (user_id);
CREATE INDEX idx_categories_user_id ON categories (user_id);
CREATE INDEX idx_transactions_user_id ON transactions (user_id);
CREATE INDEX idx_transactions_category_id ON transactions (category_id);
CREATE INDEX idx_transactions_date ON transactions (transaction_date);
CREATE INDEX idx_transactions_user_date ON transactions (user_id, transaction_date);

