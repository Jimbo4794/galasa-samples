package com.example.myapp.manager.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This class is used to group the all the annotations owned by this manager under a single annotation. This
 * Allows for easier searching from within the Manager intialise method.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface MyAppManagerField {

}