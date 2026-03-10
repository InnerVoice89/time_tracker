package com.tracker.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;


/**
 * Конфигурация соединения с БД
 */
@RequiredArgsConstructor
public class ConnectionConfig {

    /**
     * Инициализация DataSource
     */
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
