package me.blueslime.bukkitmeteor.implementation.data;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, CONSTRUCTOR, PARAMETER})
public @interface Implement {
    String identifier() default "";
}
