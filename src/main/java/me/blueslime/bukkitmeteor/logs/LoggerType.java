package me.blueslime.bukkitmeteor.logs;

public enum LoggerType {
    INFO,
    WARN,
    DEBUG,
    ERROR;

    public String getDefaultPrefix(String pluginName) {
        return switch (this) {
            default -> "&9" + pluginName + " Logs: &f";
            case WARN -> "&6" + pluginName + " Logs: &f";
            case DEBUG -> "&b" + pluginName + " Logs: &f";
            case ERROR -> "&4" + pluginName + " Logs: &f";
        };
    }
}
