package com.tracker;

import com.tracker.models.User;

public class UserContext {

    private static final ThreadLocal<User> authUser = new ThreadLocal<>(); //!!!Очистить!!!

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
