package com.tracker.security.authentication;


import com.tracker.dto.User;
import com.tracker.exceptions.SecurityException;
import com.tracker.security.PasswordEncoder;
import com.tracker.services.UserService;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    public User authenticate(String username, String password) throws SQLException {

        User user = userService.findUserByUsername(username);
        if (user == null)
            throw new RuntimeException("Пользователь не найден");
        if (!PasswordEncoder.matches(password, user.getPassword()))
            throw new SecurityException("Пароли не совпадают");
        user.setPassword(null);
        return user;

    }
}
