package viewstack.requirement;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import viewstack.stack.ViewStack;

/**
 * This annotation is used to show that
 * this view requires some other views to be
 * in {@link ViewStack} to function properly.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredViews {
    Class[] visible() default {};
    Class[] dependencies() default {};
}
