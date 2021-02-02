package com.example.myapp.manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.myapp.manager.internal.MyAppManagerField;

import dev.galasa.framework.spi.ValidAnnotatedFields;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@MyAppManagerField
@ValidAnnotatedFields({ IMyApp.class })
public @interface MyApp {

    /**
     * A tag that allows us to label a instance of my application with this annotation. 
     * This means I can have multiples running and still refernce independantly.
     */
    public String tag() default "PRIMARY";

    /**
     * A tag that allows for different versions of my application to be created.
     */
    public String version() default "latest";

}