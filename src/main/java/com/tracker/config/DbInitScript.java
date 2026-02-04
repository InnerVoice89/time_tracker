package com.tracker.config;


import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DbInitScript {


    public static void init(DataSource dataSource) throws SQLException {
        String createTaskTableSQL = "create table if not exists" +
                " task_table (" +
                "id bigint primary key generated always as identity," +
                "task_name varchar(255) not null," +
                "username varchar(255) not null ," +
                "start_task timestamp not null," +
                "end_task timestamp)";
        String createUserTableSQL = "create table if not exists user_table(" +
                "id bigint primary key generated always as identity," +
                "username varchar(255) not null unique," +
                "password varchar(255) not null," +
                "timezone varchar(255))";
        String createUserRolesSQL = "create table if not exists user_roles(" +
                "role varchar(255)," +
                "user_id bigint," +
                "constraint fk_user " +
                "FOREIGN KEY (user_id) " +
                "REFERENCES user_table(id) " +
                "on delete cascade" +
                ")";
        try (Connection connection = dataSource.getConnection();
             Statement st = connection.createStatement()
        ) {
            connection.setAutoCommit(false);
            try {
                st.execute(createUserTableSQL);
                st.execute(createTaskTableSQL);
                st.execute(createUserRolesSQL);
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
            }
        }

    }
}
