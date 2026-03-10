package com.tracker.config;

import com.tracker.exceptions.ConfigLoaderException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *  Класс получения конфигураций
 */
public class ConfigLoader {
    private final Properties props = new Properties();

    public ConfigLoader(String fileName) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (is != null)
                props.load(is);
        } catch (IOException e) {
            throw new ConfigLoaderException("Ошибка загрузки ресурсов");
        }
    }

    /**
     *  Получение значения по ключу конфиг-файла
     * @param key ключ из конфиг-файла
     * @return если отсутствует переменная окружения,то значение берется из конфига
     */
    public String get(String key) {
        String envKey = key.toUpperCase().replace(".", "_");
        String envValue = System.getenv(envKey);
        if (envValue != null)
            return envValue;
        return props.getProperty(key);
    }
}
