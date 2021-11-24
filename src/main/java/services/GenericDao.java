package services;

import models.annotation.Grim;
import models.annotation.PRIMARYKEY;
import models.exceptions.GrimException;
import java.sql.*;
import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.*;

/**
 * @author rvora
 * @Date 11/16/2021
 * @version 1
 */


public class GenericDao<T>
{
    /**
     * @param Classy
     * @param obj
     * @return
     * @throws IllegalAccessException
     */
    public boolean createTable(Class<?> Classy, T obj) throws IllegalAccessException
    {
        if (!Orm.isClassValid(Classy))
        {
            return false;
        }
        String tableName = Classy.getSimpleName();
        StringBuilder hfQuery = new StringBuilder();
        StringBuilder hfQuery2 = new StringBuilder();
        Field[] fields = Classy.getDeclaredFields();
        hfQuery.append("CREATE TABLE IF NOT EXISTS \"").append(tableName).append("\"(");
        for (Field field : fields)
        {
            if (Arrays.toString(field.getAnnotations()).contains("PrimaryKey"))
            {
                hfQuery2.append(field.getName()).append(" ");
                if (field.getDeclaredAnnotation(PRIMARYKEY.class).isSerial())
                    hfQuery2.append("serial");
                else
                    hfQuery2.append(Orm.convertType(field.getType()));
                hfQuery2.append(" primary key");
            }
        }
        for (Field field : fields)
        {
            String fieldAnnotations = Arrays.toString(field.getAnnotations());
            if (!fieldAnnotations.contains("PrimaryKey") && (fieldAnnotations.contains("Column") ||
                    fieldAnnotations.contains("NotNull") || fieldAnnotations.contains("Unique")))
            {
                if (hfQuery2.length() != 0)
                {
                    hfQuery.append(", ");
                }
                hfQuery2.append("\"").append(field.getName()).append("\" ").
                        append(Orm.convertType(field.getType())).append(" ");
                if (fieldAnnotations.contains("Unique")) {
                    hfQuery2.append("Unique ");
                }
                if (fieldAnnotations.contains("NotNull"))
                {
                    hfQuery2.append("not null ");
                }
            }
        }
        hfQuery2.append(");");
        return true;
    }

    /**
     *
     * @param Classy
     * @param matchingValues
     * @param matchingKeys
     * @param connection
     * @return
     * @throws GrimException
     */

    public List<?> read(Class<?> Classy, Object[] matchingValues, Field[] matchingKeys, Connection connection)
            throws GrimException
    {
        List<T> val = new ArrayList<>();
        String tbleName;
        if (matchingValues.length != matchingKeys.length)
        {
            throw new GrimException();
        }
        if (Classy.isAnnotationPresent(Grim.class))
        {
            tbleName = Classy.getDeclaredAnnotation(Grim.class).table();
        }
        else
        {
            tbleName = Classy.getSimpleName();
        }
        StringBuilder key = new StringBuilder();
        java.lang.reflect.Field[] fields = Classy.getDeclaredFields();
        String query = "SELECT * FROM " + tbleName +  " WHERE "+ key.toString();;
        try (Connection connect = DBConnector.getConnection();
             PreparedStatement pstmt =  connection.prepareStatement(query); )
        {
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next())
            {
                Constructor<?> construct = null;
                construct = Arrays.stream(Classy.getDeclaredConstructors())
                        .filter(c -> c.getParameterCount() == 0)
                        .findFirst().orElse(null);
                if (construct != null)
                {
                    return (List<?>) construct.newInstance();
                }
                T obj = (T) construct.newInstance();
                int location = 1;
                for (Field field : fields)
                {
                    if (Orm.convertType(field.getType()).equals(""))
                    {
                        continue;
                    }
                    field.setAccessible(true);
                    field.set(obj, resultSet.getObject(location++));
                }
                    val.add(obj);
            }
        }
        catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return val;
    }

    /**
     * @param Classy
     * @param connect
     * @return
     */
    public List<?> readAll(Class<?> Classy, Connection connect)
    {
        List<T> printAllInfo = new ArrayList<>();
        String tbleName = Classy.getSimpleName();
        if (Classy.isAnnotationPresent(Grim.class))
        {
            tbleName= Classy.getDeclaredAnnotation(Grim.class).table();
        }
        Field[] fields = Classy.getDeclaredFields();
        String query = "SELECT * FROM " + tbleName;
        try (Connection connection = DBConnector.getConnection();
             PreparedStatement pstmt =  connection.prepareStatement(query); )
        {
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next())
            {
                Constructor<?> constructor = Arrays.stream(Classy.getDeclaredConstructors())
                        .filter(x->x.getParameterCount() == 0).findFirst().orElse(null);
                constructor.setAccessible(true);
                T obj = (T) constructor.newInstance();
                int location = 1;
                for (Field field:fields)
                {
                    if (Orm.convertType(field.getType()).equals("")){continue;}
                    field.setAccessible(true);
                    field.set(obj,resultSet.getObject(location++));
                }
                printAllInfo.add(obj);
            }
        }
        catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return printAllInfo;
    }

    /**
     * @param Classy
     * @param changeValues
     * @param changeKeys
     * @param matchingValues
     * @param matchingKeys
     * @return
     * @throws GrimException
     */

    public List<T> update(Class<?> Classy, Object[] changeValues, Field[] changeKeys,
                          Object[] matchingValues, Field[] matchingKeys) throws GrimException
    {
        List<T> output = null;
        String tbleName;
        if (matchingValues.length != matchingKeys.length)
        {
            throw new GrimException();
        }
        if (changeValues.length != changeKeys.length)
        {
            throw new GrimException();
        }
        if (Classy.isAnnotationPresent(Grim.class))
        {
            tbleName = Classy.getDeclaredAnnotation(Grim.class).table();
        }
        else
        {
            tbleName = Classy.getSimpleName();
        }
        StringBuilder pKey = new StringBuilder();
        StringBuilder columns = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        String query = "UPDATE "+ tbleName + " SET " + columns + "=" + vals + " WHERE " +
                pKey.toString();
        try (Connection connection = DBConnector.getConnection();
             PreparedStatement pstmt =  connection.prepareStatement(query); )
        {
            int location = 1;
            for (Object o:changeValues)
            {
                if (Orm.convertType(o.getClass()).equals(""))
                {
                    continue;
                }
                pstmt.setObject(location++, o);
            }
            for (Object o:matchingValues)
            {
                if (Orm.convertType(o.getClass()).equals(""))
                {
                    continue;
                }
                pstmt.setObject(location++, o);
            }
            System.out.println(pstmt);
            pstmt.executeUpdate();
            ResultSet resultSet = pstmt.getResultSet();
            output = new ArrayList<>();
            while (resultSet.next()) {
                Constructor<?> constructor = Arrays.stream(Classy.getDeclaredConstructors()).
                        filter(x -> x.getParameterCount() == 0).findFirst().orElse(null);
                constructor.setAccessible(true);
                T obj = (T) constructor.newInstance();
                int i = 1;
                for (Field field : obj.getClass().getDeclaredFields())
                {
                    field.set(obj, resultSet.getObject(i++));
                }
                output.add(obj);
            }
        }
        catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return output;
    }

    /**
     *
     * @param Classy
     * @param matchingValues
     * @param matchingKeys
     * @return
     * @throws GrimException
     */
    public int delete(Class<?> Classy, Object[] matchingValues, Field[] matchingKeys, Connection connect)
            throws GrimException
    {
        if (matchingValues.length != matchingKeys.length)
        {
            throw new GrimException();
        }
        int val = -1;
        String tbleName;
        if (Classy.isAnnotationPresent(Grim.class))
        {
            tbleName = Classy.getDeclaredAnnotation(Grim.class).table();
        }
        else
        {
            tbleName = Classy.getSimpleName();
        }
        java.lang.reflect.Field[] fiel = Classy.getDeclaredFields();
        StringBuilder key = new StringBuilder();
        String query = "DELETE FROM " + tbleName + " WHERE " + key.toString();
        try ( PreparedStatement pstmt =  connect.prepareStatement(query); )
        {
            int loc = 1;
            for (Object o : matchingValues)
            {
                if (Orm.convertType(o.getClass()).equals(""))
                {
                    continue;
                }
                 pstmt.setObject(loc++, o);
            }
            val = pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return val;
    }

    public static <T> void getAnnotation(Class<T> clazz)
    {
        java.lang.reflect.Field[] fields = clazz.getFields();
        for (java.lang.reflect.Field field : fields)
        {
            System.out.println("Annotation: " + Arrays.toString(field.getAnnotations()));
        }
    }
}