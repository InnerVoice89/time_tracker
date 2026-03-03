package com.tracker.services;

import com.tracker.dao.UserDao;
import com.tracker.dto.User;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final DataSource dataSource;

    public User findUserByUsername(String username) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return userDao.findUserByUsername(username, connection);
        }
    }
}


