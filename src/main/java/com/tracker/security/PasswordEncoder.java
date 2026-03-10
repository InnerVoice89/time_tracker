package com.tracker.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Утилитарный класс для хэширования и проверки паролей.
 * Использует алгоритм {@link BCrypt} для безопасного хранения паролей.
 */
public class PasswordEncoder {

    /**
     * Создаёт хэш пароля.
     *
     * @param raw пароль в открытом виде
     * @return строка с хэшем пароля
     */
    public static String hash(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt());
    }

    /**
     * Проверяет совпадение введённого пароля с сохранённым хэшем.
     * @param raw     пароль в открытом виде
     * @param encoded сохранённый хэш пароля
     * @return true, если пароль совпадает с хэшем, иначе false
     */
    public static boolean matches(String raw, String encoded) {
        return BCrypt.checkpw(raw, encoded);
    }

}
