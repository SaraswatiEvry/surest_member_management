-- Create role table
CREATE TABLE role
(
    id   UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Create user table
CREATE TABLE "user"
(
    id            UUID PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id       UUID         NOT NULL,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role (id)
);

-- Create member table
CREATE TABLE member
(
    id            UUID PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    date_of_birth DATE         NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);