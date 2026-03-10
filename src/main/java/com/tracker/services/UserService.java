package com.tracker.services;

import com.tracker.config.TransactionManager;
import com.tracker.dao.UserDao;
import com.tracker.dto.User;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

/**
 * Сервис для работы с пользователями
 */
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final TransactionManager transactionManager;

    /**
     * Создание пользователя
     */
    public void createUser(User user) throws SQLException {
        transactionManager.executeInTransaction(connection -> {
            userDao.createUser(user, connection);
            return null;
        });
    }

    /**
     * Изменение пользователя
     */
    public void updateUser(User user) throws SQLException {
        transactionManager.executeInTransaction(connection -> {
            userDao.updateUser(user, connection);
            return null;

        });
    }

    /**
     * Удаление пользователя
     */
    public void deleteUser(long id) throws SQLException {
        transactionManager.executeInTransaction(connection -> {
            userDao.deleteUserById(id, connection);
            return null;
        });
    }

    /**
     * Поиск пользователя по username
     */
    public User findUserByUsername(String username) throws SQLException {
        return transactionManager.executeRead(connection ->
            userDao.findUserByUsername(username, connection)
        );
    }
}



