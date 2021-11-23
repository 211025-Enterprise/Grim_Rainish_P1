package models.annotation;

import models.constraints.GrimDBConstraints;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

/**
 *
 */
public @interface Field
{
    public String Name();
    GrimDBConstraints[] constraints();
}
