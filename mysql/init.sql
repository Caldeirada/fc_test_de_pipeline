CREATE DATABASE IF NOT EXISTS local_env;
USE local_env;
CREATE TABLE daily_user_country_platform (
    platform VARCHAR(50) NOT NULL,
    country_name VARCHAR(50) NOT NULL,
    ref_date DATE NOT NULL,
    record_count INT,
    PRIMARY KEY (platform, country_name, ref_date)
);
CREATE TABLE daily_user_country_platform_temp (
    platform VARCHAR(50) NOT NULL,
    country_name VARCHAR(50) NOT NULL,
    ref_date DATE NOT NULL,
    record_count INT
);