package services;

import models.annotation.Field;
import models.exceptions.GrimException;

import java.sql.Connection;
import java.util.List;

/**
 * @author rvora
 */
public class Grimmace
{
    private static Connection connect;

    public static Object add(Object object)
    {
        int i = -1;
        Object output = null;
        try
        {
            output = new GenericDao().createTable(object.getClass(),object);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    public static List<?> get(Class<?> classy, Object[] matchValues, Field[] matchKeys) throws GrimException
    {
        int i = -1;
        List<?> list = null;
        String[] chars = new String[matchKeys.length];
        for (int j = 0; j < chars.length; j++)
        {
            try
            {
                chars[j] = "=";
                list = new GenericDao().read(classy, matchValues, matchKeys, connect);

            }
            catch (GrimException e)
            {
                e.printStackTrace();
            }
            return list;
        }
        return null;
    }

    public static Integer update(Class<?> clazz, Object[] changeValues, Field[] changeKeys,
                                 Object[] matchValues, Field[] matchKeys)
    {
        int i = -1;
        int output = 0;
        try
        {
            output = new GenericDao().update(clazz, changeValues, changeKeys, matchValues, matchKeys);
            return output;
        }
        catch (GrimException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    public static int delete(Class<?> clazz, Object[] matchValues, Field[] matchKeys)
    {
        int i = -1;
        int output = -1;
        try
        {
            output = new GenericDao().delete(clazz, matchValues, matchKeys);
            return output;
        }
        catch (GrimException e)
        {
            e.printStackTrace();
        }
        return -1;
    }
}
