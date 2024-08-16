package me.blueslime.bukkitmeteor.logs;

public enum LoggerType {
    INFO,
    WARN,
    DEBUG,
    ERROR;

    public String getDefaultPrefix(String pluginName) {
        switch (this) {
            default:
            case INFO:
                return "&9" + pluginName + " Logs: &f";
            case WARN:
                return "&6" + pluginName + " Logs: &f";
            case DEBUG:
                return "&b" + pluginName + " Logs: &f";
            case ERROR:
                return "&4" + pluginName + " Logs: &f";
        }
    }
}
