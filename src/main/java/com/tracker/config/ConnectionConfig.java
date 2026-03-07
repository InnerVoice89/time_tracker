package com.tracker.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
public class ConnectionConfig {

    public DataSource dataSource(ConfigLoader configLoader) {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        config.setJdbcUrl(configLoader.get("db.url"));
        config.setUsername(configLoader.get("db.username"));
        config.setPassword(configLoader.get("db.password"));
        return new HikariDataSource(config);
    }
}
