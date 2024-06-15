package me.blueslime.bukkitmeteor.logs;

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
        String prefix = getPrefix(LoggerType.ERROR);
        Class<?> current = exception.getClass();
        String location = current.getName();
        String error = current.getSimpleName();
        send(prefix + " -------------------------");
        send(prefix + "Location: " + location.replace("." + error,""));
        send(prefix + "Error: " + error);

        if (exception.getStackTrace() != null) {
            send(prefix + "StackTrace: ");

            for (StackTraceElement line : exception.getStackTrace()) {
                String convertedLine = (prefix + " (Line: "
                        + line.getLineNumber() + ") (Class: " + line.getFileName() + ") (Method: "
                        + line.getMethodName() + ")")
                        .replace(".java","");

                send(
                        convertedLine
                );
            }
        }
        send(prefix + " -------------------------");
    }

    MeteorLogger setPrefix(LoggerType log, String prefix);

    String getPrefix(LoggerType prefix);

    void build();
}
