package me.blueslime.bukkitmeteor.logs;

import me.blueslime.bukkitmeteor.implementation.Implements;

public interface MeteorLogger {

    default void error(String... messages) {
        String prefix = getPrefix(LoggerType.ERROR);

        for (String message : messages) {
            send(prefix + message);
        }
    }

    default void error(Exception exception) {
        printException(exception);
    }

    default void error(Exception exception, String... messages) {
        String prefix = getPrefix(LoggerType.ERROR);

        for (String message : messages) {
            send(prefix + message);
        }

        printException(exception);
    }

    default void error(Throwable exception) {
        printThrowable(exception);
    }

    default void error(Throwable exception, String... messages) {
        String prefix = getPrefix(LoggerType.ERROR);

        for (String message : messages) {
            send(prefix + message);
        }

        printThrowable(exception);
    }

    default void warn(String... messages) {
        String prefix = getPrefix(LoggerType.WARN);

        for (String message : messages) {
            send(prefix + message);
        }
    }

    default void debug(String... messages) {
        String prefix = getPrefix(LoggerType.DEBUG);

        for (String message : messages) {
            send(prefix + message);
        }
    }

    default void info(String... messages) {
        String prefix = getPrefix(LoggerType.INFO);

        for (String message : messages) {
            send(prefix + message);
        }
    }

    void send(String... message);

    default void printException(Exception exception) {
        printLog(exception);
    }

    default void printThrowable(Throwable throwable) {
        printLog(throwable);
    }

    default void printLog(Throwable throwable) {
        String prefix = getPrefix(LoggerType.ERROR);
        Class<?> current = throwable.getClass();
        String location = current.getName();
        String error = current.getSimpleName();
        String message = throwable.getMessage() != null ? throwable.getMessage() : "No message available";

        send(prefix + " -------------------------");
        send(prefix + "Location: " + location.replace("." + error, ""));
        send(prefix + "Error: " + error);
        send(prefix + "Message: " + message);

        if (throwable.getCause() != null) {
            send(prefix + "Cause: " + throwable.getCause().toString());
        }

        send(prefix + "StackTrace: ");
        for (StackTraceElement line : throwable.getStackTrace()) {
            String className = line.getClassName();
            int lastDotIndex = className.lastIndexOf('.');
            String packageName = lastDotIndex != -1 ? className.substring(0, lastDotIndex) : "(default package)";
            String convertedLine = (prefix + " (Line: "
                    + line.getLineNumber() + ") (Class: " + line.getFileName() + ") (Method: "
                    + line.getMethodName() + ") (In: " + className + ") (Package: " + packageName + ")")
                    .replace(".java", "");

            send(convertedLine);
        }
        send(prefix + " -------------------------");
    }

    MeteorLogger setPrefix(LoggerType log, String prefix);

    String getPrefix(LoggerType prefix);

    void build();

    /**
     * Gets the logger
     * @return logger instance
     */
    static MeteorLogger fetch() {
        return Implements.fetch(MeteorLogger.class);
    }
}
