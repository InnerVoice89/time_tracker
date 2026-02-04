package com.tracker.security.authentication;


import com.tracker.models.User;
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
            String encodedPassword = PasswordEncoder.hash(password);
            if (!PasswordEncoder.matches(encodedPassword, user.getPassword()))
                return null;
            user.setPassword(null);
            return user;

    }
}
