CREATE TABLE app_user (
                          id BIGSERIAL PRIMARY KEY,
                          oauth_provider VARCHAR(255) NOT NULL,
                          provider_id VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          display_name VARCHAR(255),
                          bio TEXT,
                          enabled BOOLEAN NOT NULL,
                          locked BOOLEAN NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL,
                          updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE categories (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            parent_id INT REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE items (
                       id BIGSERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       price_amount NUMERIC(10,2) NOT NULL,
                       price_unit VARCHAR(50) NOT NULL,
                       owner_id BIGINT NOT NULL REFERENCES app_user(id),
                       created_at TIMESTAMPTZ NOT NULL,
                       updated_at TIMESTAMPTZ NOT NULL,
                       latitude NUMERIC(10,7),
                       longitude NUMERIC(10,7),
                       address VARCHAR(255) NOT NULL,
                       category_id INT REFERENCES categories(id)
);

CREATE TABLE images (
                        id BIGSERIAL PRIMARY KEY,
                        data bytea NOT NULL,
                        filename VARCHAR(255) NOT NULL,
                        content_type VARCHAR(100) NOT NULL,
                        size BIGINT NOT NULL,
                        is_thumbnail BOOLEAN NOT NULL,
                        uploaded_at TIMESTAMPTZ NOT NULL,
                        item_id BIGINT NOT NULL REFERENCES items(id)
);

CREATE TABLE user_roles (
                            app_user_id BIGINT NOT NULL REFERENCES app_user(id),
                            role VARCHAR(100) NOT NULL,
                            PRIMARY KEY (app_user_id, role)
);
