CREATE DATABASE IF NOT EXISTS local_env;
USE local_env;
CREATE TABLE daily_user_country_platform (
    platform VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    record_count INT,
    PRIMARY KEY (platform, country, date)
);