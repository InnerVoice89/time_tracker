package com.tracker.config;

import com.tracker.exceptions.PersistenceException;
import com.tracker.security.PasswordEncoder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
public class ConnectionConfig {

    public Properties getProperties(String propertyName) {
        Properties props = new Properties();
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(propertyName)) {
            if (is == null)
                throw new RuntimeException("Ошибка обработки ресурсов");
            props.load(is);
            return props;
        } catch (IOException ex) {  //Логгер!!!!
            throw new IllegalStateException("Не удалось загрузить конфигурацию", ex);
        }
    }

    public DataSource dataSource(Properties properties) {

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(10);
        config.setJdbcUrl(properties.getProperty("spring.datasource.url"));
        config.setUsername(properties.getProperty("spring.datasource.username"));
        config.setPassword(properties.getProperty("spring.datasource.password"));
        return new HikariDataSource(config);
    }

    public static void initDataBase(DataSource dataSource) throws SQLException {
        String createTaskTableSQL = "create table if not exists task_table (" +
                "id bigint primary key generated always as identity," +
                "task_name varchar(255) not null," +
                "user_id bigint not null ," +
                "start_task timestamp not null," +
                "end_task timestamp)";
        String createUserTableSQL = "create table if not exists user_table (" +
                "id bigint primary key generated always as identity," +
                "username varchar(255) not null unique," +
                "password varchar(255) not null," +
                "timezone varchar(255))";
        String createUserRolesSQL = "create table if not exists user_roles (" +
                "role varchar(255) not null default 'USER'," +
                "user_id bigint not null ," +
                "primary key(role,user_id)," +
                "constraint fk_user " +
                "FOREIGN KEY (user_id) " +
                "REFERENCES user_table(id) " +
                "on delete cascade" +
                ")";
        String password = PasswordEncoder.hash("admin");
        String createAdminSQL = "insert into user_table(username,password,timezone) values(?,?,?) " +
                "on conflict(username) do update set username=excluded.username RETURNING id";
        String insertRoleSql = "insert into user_roles(role, user_id) " +
                "values (?,?) on conflict(role,user_id) do nothing ";
        try (Connection connection = dataSource.getConnection();
             Statement st = connection.createStatement();
             PreparedStatement createAdminPs = connection.prepareStatement(createAdminSQL);
             PreparedStatement insertRolesPs = connection.prepareStatement(insertRoleSql)
        ) {
            st.execute(createUserTableSQL);
            st.execute(createTaskTableSQL);
            st.execute(createUserRolesSQL);

            connection.setAutoCommit(false);
            try {
                createAdminPs.setString(1, "admin");
                createAdminPs.setString(2, password);
                createAdminPs.setString(3, "Europe/Moscow");

                ResultSet rs = createAdminPs.executeQuery();
                long id;
                if (rs.next()) {
                    id = rs.getLong("id");
                } else {
                    throw new PersistenceException("Проблема с генерацией id");
                }
                insertRolesPs.setString(1, "ADMIN");
                insertRolesPs.setLong(2, id);
                insertRolesPs.executeUpdate();

                connection.commit();
            } catch (Exception ex) {
                log.warn("Ошибка при создании тестового админа ", ex);
                connection.rollback();
                throw new PersistenceException("Не удалось инициализировать базу", ex);

            }
        }

    }
}
