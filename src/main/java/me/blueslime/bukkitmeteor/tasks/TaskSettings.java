package me.blueslime.bukkitmeteor.tasks;

public class TaskSettings {

    private TaskType type = TaskType.NORMAL;
    private boolean async = true;
    private long period = 0L;
    private long delay = 0L;

    private TaskSettings() {

    }

    public boolean isAsync() {
        return async;
    }

    public TaskSettings setAsync(boolean async) {
        this.async = async;
        return this;
    }

    public long getDelay() {
        return delay;
    }

    public TaskSettings setDelay(long delay) {
        this.delay = delay;
        return this;
    }

    public long getPeriod() {
        return period;
    }

    public TaskSettings setPeriod(long period) {
        this.period = period;
        return this;
    }

    public TaskType getType() {
        return type;
    }

    public TaskSettings setType(TaskType type) {
        this.type = type;
        return this;
    }

    public static TaskSettings create() {
        return new TaskSettings();
    }
}
