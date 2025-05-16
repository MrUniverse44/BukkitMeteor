package me.blueslime.bukkitmeteor.commands.creator.arguments;

import me.blueslime.bukkitmeteor.commands.creator.interfaces.ArgumentHandler;
import me.blueslime.bukkitmeteor.commands.sender.Sender;

import java.util.HashSet;
import java.util.Set;

public class EmptyMethodCommandHandler implements ArgumentHandler {

    private final Set<TypedExistentArgument.RunnableMethod> methodHandlers = new HashSet<>();

    public static EmptyMethodCommandHandler create() {
        return new EmptyMethodCommandHandler();
    }

    public EmptyMethodCommandHandler add(TypedExistentArgument.RunnableMethod method) {
        methodHandlers.add(method);
        return this;
    }

    public EmptyMethodCommandHandler remove(TypedExistentArgument.RunnableMethod method) {
        methodHandlers.remove(method);
        return this;
    }

    @Override
    public boolean handle(Sender sender, String[] args) {
        if (args.length == 0) {
            methodHandlers.forEach(methods -> methods.invoke(sender));
            return true;
        }
        return false;
    }
}
