package com.tracker.config;


import com.tracker.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Менеджер транзакций для работы с базой данных
 * Предоставляет методы для выполнения SQL операций с управлением транзакцией:
 * автоматически открывает соединение, выполняет действие и коммитит или откатывает транзакцию в случае ошибки.
 */
@RequiredArgsConstructor
public class TransactionManager {

    private final DataSource dataSource;

    /**
     * выполняет действие с транзакцией и коммитом/роллбеком
     */
    public <T> T executeInTransaction(TransactionAction<Connection, T> action) {
        try (Connection connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                T result = action.apply(connection);
                connection.commit();
                return result;
            } catch (Exception e) {
                connection.rollback();
                throw new BusinessException("Ошибка обработки запроса", e);
            }
        } catch (SQLException ex) {
            throw new BusinessException("Ошибка при соединении с БД", ex);
        }
    }

    /**
     * выполняет действие без транзакции, только с открытым соединением
     */
    public <T> T executeRead(TransactionAction<Connection, T> action) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return action.apply(connection);

        } catch (SQLException ex) {
            throw new BusinessException("Ошибка при соединении с БД", ex);
        } catch (Exception e) {
            throw new BusinessException("Ошибка обработки запроса", e);
        }
    }

    @FunctionalInterface
    public interface TransactionAction<T, R> {
        R apply(T t) throws SQLException;
    }
}
