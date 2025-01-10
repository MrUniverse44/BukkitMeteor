package me.blueslime.bukkitmeteor.commands.advanced;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Argument {
    String name();
    String[] aliases() default {};
    boolean hasPermission() default false;
    String permission() default "";
}

