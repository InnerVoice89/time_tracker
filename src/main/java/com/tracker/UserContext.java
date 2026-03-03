package com.tracker;

import com.tracker.dto.User;

public class UserContext {

    private static final ThreadLocal<User> authUser = new ThreadLocal<>();

    public static User getUser() {
        return authUser.get();
    }

    public static void setUser(User user) {
        authUser.set(user);
    }

    public static void clean(){
        authUser.remove();
    }

}
