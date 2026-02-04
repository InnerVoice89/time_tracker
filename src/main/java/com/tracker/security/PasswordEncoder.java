package com.tracker.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordEncoder {

    public static String hash(String raw){
     return BCrypt.hashpw(raw,BCrypt.gensalt());
    }

    public static boolean matches(String raw,String encoded){
       return BCrypt.checkpw(raw, encoded);
    }

}
