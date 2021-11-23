package models.annotation;
import models.annotation.Field;

import java.lang.reflect.Constructor;
import java.util.*;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)

/**
 *
 */
public @interface Grim
{
     String table();

}
