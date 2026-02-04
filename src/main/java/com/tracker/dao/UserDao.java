package com.tracker.dao;

import com.tracker.dto.Role;
import com.tracker.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class UserDao {

    public User findUserByUsername(String username, Connection connection) throws SQLException {
        String sql = "select * from user_table ut join user_roles ur on ut.id=ur.user_id " +
                "where ut.username=?";
        User user = null;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (user == null) {
                    user = new User();
                    Set<Role> roles = new HashSet<>();
                    user.setRoles(roles);
                    user.setUsername(rs.getString("username"));
                    user.setTimeZone(rs.getString("timezone"));
                    user.setPassword(rs.getString("password"));
                    user.getRoles().add(Role.valueOf(rs.getString("role")));
                } else
                    user.getRoles().add(Role.valueOf(rs.getString("role")));

            }

        }
        return user;
    }

}
