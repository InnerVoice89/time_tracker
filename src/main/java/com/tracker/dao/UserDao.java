package com.tracker.dao;

import com.tracker.dto.Role;
import com.tracker.dto.User;
import com.tracker.exceptions.PersistenceException;
import com.tracker.security.PasswordEncoder;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO-класс для работы с пользователями
 */
public class UserDao {
    /**
     * Поиск пользователя по имени
     */
    public User findUserByUsername(String username, Connection connection) {
        return findUserBy(connection, "username", username);
    }

    /**
     * Поиск пользователя по идентификатору
     */
    public User findUserById(long id, Connection connection) {
        return findUserBy(connection, "id", id);
    }

    /**
     * Универсальный метод поиска пользователя по указанному столбцу.
     * Допустимые значения параметра column: id, username
     */
    public User findUserBy(Connection connection, String column, Object value) {
        if (!List.of("id", "username").contains(column))
            throw new IllegalArgumentException("Некорректное имя столбца");
        String sql = "select ut.id, ut.username, ut.password, ut.timezone, ur.role " +
                "from user_table ut left join user_roles ur on ut.id=ur.user_id " +
                "where ut." + column + "=?";
        User user = null;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, value);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (user == null) {
                    user = new User();
                    Set<Role> roles = new HashSet<>();
                    user.setRoles(roles);
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setTimeZone(rs.getString("timezone"));
                    user.setPassword(rs.getString("password"));
                    user.getRoles().add(Role.valueOf(rs.getString("role")));
                } else
                    user.getRoles().add(Role.valueOf(rs.getString("role")));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Ошибка получения информации о пользователе", e);
        }
        return user;
    }

    /**
     * Создание нового пользователя
     */
    public void createUser(User user, Connection connection) {

        try (PreparedStatement ps = connection.prepareStatement("insert into user_table (" +
                "username,password,timezone) values (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordEncoder.hash(user.getPassword()));
            ps.setString(3, user.getTimeZone());
            int affectedRow = ps.executeUpdate();
            if (affectedRow == 0)
                throw new PersistenceException("Создание пользователя не удалось, строка не добавлена");
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    if (user.getRoles() == null)
                        user.setRoles(Set.of(Role.USER));
                    addRolesToId(user.getRoles(), id, connection);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Ошибка создания пользователя", e);
        }
    }

    /**
     * Добавление ролей пользователю
     */
    public void addRolesToId(Set<Role> roles, long id, Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement("insert into  user_roles" +
                "(role,user_id) values(?,?) on conflict (role, user_id) do nothing ")) {
            for (Role role : roles) {
                ps.setString(1, role.name());
                ps.setLong(2, id);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new PersistenceException("Ошибка добавления роли пользователю", e);
        }
    }

    /**
     * Обновление данных пользователя
     */
    public void updateUser(User user, Connection connection) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("Id не может быть пустым");
        }
        List<String> attributes = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (user.getUsername() != null) {
            attributes.add("username = ?");
            params.add(user.getUsername());
        }
        if (user.getPassword() != null) {
            attributes.add("password = ?");
            params.add(PasswordEncoder.hash(user.getPassword()));
        }
        if (user.getTimeZone() != null) {
            attributes.add("timezone = ?");
            params.add(user.getTimeZone());
        }
        if (!params.isEmpty()) {
            String attributesStr = String.join(",", attributes);
            params.add(user.getId());
            String sql = "update user_table set " + attributesStr + " where id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0)
                    throw new PersistenceException("Пользователь не найден");
            } catch (SQLException e) {
                throw new PersistenceException("Ошибка обновления пользователя", e);
            }
        }
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            updateRoles(user.getRoles(), connection, user.getId());
        }
    }

    /**
     * Обновление ролей пользователя
     */
    public void updateRoles(Set<Role> roles, Connection connection, long id) {
        try {
            try (PreparedStatement ps = connection.prepareStatement(
                    "select 1 from user_table where id = ? for update ")) {
                ps.setLong(1, id);
                ps.executeQuery();
            }
            try (PreparedStatement deletePs = connection.prepareStatement("delete from user_roles " +
                    "where user_id=?")) {
                deletePs.setLong(1, id);
                deletePs.executeUpdate();
            }
            String updateRoles = "insert into user_roles (role,user_id) values(?,?)";
            try (PreparedStatement updatePs = connection.prepareStatement(updateRoles)) {
                for (Role role : roles) {
                    updatePs.setString(1, role.name());
                    updatePs.setLong(2, id);
                    updatePs.addBatch();
                }
                updatePs.executeBatch();
            }
        }catch (SQLException e) {
            throw new PersistenceException("Ошибка обновления ролей пользователя", e);
        }
    }

    /**
     * Удаление роли пользователя
     */
    public void deleteRoleById(long id, Role role, Connection connection) {
        String sql = "delete from user_roles where user_id=? and role=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setString(2, role.name());
            ps.executeUpdate();
        }catch (SQLException e) {
            throw new PersistenceException("Ошибка удаления роли пользователя", e);
        }
    }

    /**
     * Удаление пользователя по идентификатору
     */
    public void deleteUserById(long id, Connection connection){
        String sql = "delete from user_table where id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }catch (SQLException e) {
            throw new PersistenceException("Ошибка удаления пользователя", e);
        }
    }
}
