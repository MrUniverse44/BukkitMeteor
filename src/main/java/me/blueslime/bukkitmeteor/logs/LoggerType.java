package me.blueslime.bukkitmeteor.logs;

public enum LoggerType {
    INFO,
    WARN,
    DEBUG,
    ERROR;

    public String getDefaultPrefix() {
        switch (this) {
            default:
            case INFO:
                return "&9Logs: &f";
            case WARN:
                return "&6Logs: &f";
            case DEBUG:
                return "&bLogs: &f";
            case ERROR:
                return "&4Logs: &f";
        }
    }
}
