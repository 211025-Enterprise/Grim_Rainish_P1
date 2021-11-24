package services;

import models.annotation.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Orm
{
    public static String convertType(Type type)
    {
        String cType = type.getTypeName();
        String oType = null;
        switch (cType)
        {
            case "char":
            case "java.lang.Character":
                oType = "char";
                break;
            case "boolean":
            case "java.lang.Boolean":
                oType = "bool";
                break;
            case "int":
            case "java.lang.Integer":
                oType = "int";
                break;
            case "float":
            case "java.lang.Float":
            case "double":
            case "java.lang.Double":
                oType = "double precision";
                break;
            case "java.lang.String":
                oType = "text";
                break;
        }
        return oType;
    }

    /**
     *
     * @param clazz
     * @param annotation
     * @return
     */
    public static Field[] getFieldsFromAnnotation(Class<?> clazz, String annotation)
    {
        List<Field> fieldList = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields())
        {
            if (Arrays.toString(field.getDeclaredAnnotations()).contains(annotation))
            {
                fieldList.add(field);
            }
        }
        return fieldList.toArray(new Field[0]);
    }

    /**
     *
     * @param clazz
     * @return
     */
    public static boolean isClassValid(Class<?> clazz)
    {
        List<Field> primaryKeyFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> Arrays.toString(field.getDeclaredAnnotations()).contains("PrimaryKey")).collect(Collectors.toList());
        return primaryKeyFields.size() == 1;
    }

    /**
     *
     * @param obj
     * @return
     */
    public static boolean isObjectValid(Object obj)
    {
        List<Field> notNullKeyFields = Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(field -> Arrays.toString(field.getDeclaredAnnotations())
                        .contains("NotNull")).collect(Collectors.toList());
        if (notNullKeyFields.size() > 0)
        {
            try
            {
                for (Field notNullKeyField : notNullKeyFields)
                {
                    notNullKeyField.setAccessible(true);
                    if (notNullKeyField.get(obj) == null)
                    {
                        return false;
                    }
                }
            }
            catch (IllegalAccessException e)
            {
                System.out.println("Variable(NotNull) can't be accessed");
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     *
     * @param clazz
     * @return
     */
    public static Object getInstance(Class<?> clazz)
    {
        Constructor<?> noConstructArgs = null;

        noConstructArgs = Arrays.stream(clazz.getDeclaredConstructors())
                .filter( x -> x.getParameterCount() == 0)
                .findFirst().orElse(null);

        if(noConstructArgs != null)
        {
            try
            {
                return noConstructArgs.newInstance();
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void deleteByID(String query)
    {
        try (Connection conn = (Connection) DBConnector.getInstance())
        {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.execute();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void dropTable(Class<?> clazz)
    {
        if (GenericDao.doesTableExist(clazz))
            GenericDao.dropTable(clazz);
    }

}

