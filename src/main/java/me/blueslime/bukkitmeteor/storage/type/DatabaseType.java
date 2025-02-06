package me.blueslime.bukkitmeteor.storage.type;

import java.util.Locale;

public enum DatabaseType {
    POSTGRE,
    MARIADB,
    MONGODB,
    JSON,
    YAML;

    public static DatabaseType fromString(String string) {
        switch (string.toLowerCase(Locale.ENGLISH)) {
            case "postgre", "postgredb", "postgres" -> {
                return POSTGRE;
            }
            case "maria", "mariadb" -> {
                return MARIADB;
            }
            case "mongodb", "mongo" -> {
                return MONGODB;
            }
            case "json" -> {
                return JSON;
            }
            default -> {
                return YAML;
            }
        }
    }
}
