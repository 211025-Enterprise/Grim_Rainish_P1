package models.annotation;

import models.constraints.GrimDBConstraints;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

/**
 *
 */
public @interface Column
{
    public String Name();
    GrimDBConstraints[] constraints();
}
