package me.blueslime.bukkitmeteor.commands.creator.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CastFailedArgument {

    Class<?> value();

    int index() default -1;

}
