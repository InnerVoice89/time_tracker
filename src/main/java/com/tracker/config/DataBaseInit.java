package com.tracker.config;

import com.tracker.exceptions.PersistenceException;
import com.tracker.security.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Класс инициализации БД
 */
public class DataBaseInit {
    /**
     * Создание необходимых таблиц и тестового администратора с username = 'admin';password = 'admin'
     */
    public static void initDataBase(DataSource dataSource) throws SQLException {
        String createTaskTableSQL = "create table if not exists task_table (" +
                "id bigint primary key generated always as identity," +
                "task_name varchar(255) not null," +
                "user_id bigint not null ," +
                "start_task timestamp not null," +
                "end_task timestamp," +
                "constraint fk_task_user " +
                "foreign key (user_id) " +
                "references user_table(id) " +
                "on delete cascade)";

        String createUserTableSQL = "create table if not exists user_table (" +
                "id bigint primary key generated always as identity," +
                "username varchar(255) not null unique," +
                "password varchar(255) not null," +
                "timezone varchar(255) not null )";

        String createUserRolesSQL = "create table if not exists user_roles (" +
                "role varchar(255) not null default 'USER'," +
                "user_id bigint not null ," +
                "primary key(role,user_id)," +
                "constraint fk_user " +
                "FOREIGN KEY (user_id) " +
                "REFERENCES user_table(id) " +
                "on delete cascade)";

        String createTimeIntervalTable = "create table if not exists time_interval_table(" +
                "id bigint primary key generated always as identity," +
                "task_id bigint not null," +
                "start_time timestamp not null," +
                "end_time timestamp," +
                "constraint fk_int_table " +
                "FOREIGN KEY (task_id) " +
                "REFERENCES task_table(id) " +
                "on delete cascade)";

// Создание индекса уникальности null значения поля end_time таблицы time_interval_table для предотвращения race_condition
        String indexUniqueNullSql = "create unique index if not exists one_active_task_per_user " +
                "ON time_interval_table(task_id) " +
                "WHERE end_time IS NULL";

        // Создается администратор
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
            connection.setAutoCommit(false);
            try {
                st.execute(createUserTableSQL);
                st.execute(createTaskTableSQL);
                st.execute(createUserRolesSQL);
                st.execute(createTimeIntervalTable);
                st.execute(indexUniqueNullSql);


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
                connection.rollback();
                throw new PersistenceException("Ошибка при инициализации базы данных ", ex);
            }
        }
    }
}
