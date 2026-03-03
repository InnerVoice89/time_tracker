package com.tracker.services;

import com.tracker.dao.UserDao;
import com.tracker.dto.User;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
public class AdminService {

    private final UserDao userDao;
    private final DataSource dataSource;

    public void createUser(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                userDao.createUser(user,connection);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
    public void updateUser(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                userDao.updateUser(user,connection);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        }


    }


}


