package services;

import models.annotation.Field;
import models.annotation.Grim;
import models.constraints.GrimDBConstraints;
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
    public int createTable(Class<?> Classy, T obj) throws IllegalAccessException
    {
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
        java.lang.reflect.Field[] fields = Classy.getDeclaredFields();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder tble = new StringBuilder();
        tble.append("CREATE TABLE IF NOT EXISTS").append(tbleName).append("\n");
        boolean pKey = false;
        for (java.lang.reflect.Field field : fields)
        {
            Field fiel = field.getAnnotation(Field.class);
            String columnname = field.getName().toLowerCase();
            if (fiel != null)
            {
                columnname = field.getName().toLowerCase();
            }
            tble.append(columnname).append(" ");
            columns.append(columnname);
            tble.append(convertType(field.getType())).append(" ");
            if (fiel != null)
            {
                GrimDBConstraints[] grimDBConstraints = fiel.constraints();
                for (GrimDBConstraints c : grimDBConstraints)
                {
                    if(c.equals(GrimDBConstraints.Unique))
                    {
                        tble.append("UNIQUE");
                    }
                    if(c.equals(GrimDBConstraints.NOTNULL))
                    {
                        tble.append("NOTNULL");
                    }
                    if(c.equals(GrimDBConstraints.PrimaryKey))
                    {
                        tble.append("PrimaryKey");
                    }
                    if (!pKey)
                    {
                        tble.append("Primary Key");
                        pKey = true;
                    }
                }
            }
            values.append("?");
            columns.append(",");
            values.append(",");
            tble.append(",\n");
        }
        String sql = tble.toString();
        String query = "INSERT INTO " + tbleName + " (" + columns + ") Values (" + values + ")";
        try (Connection connection= DBConnector.getConnection();
             PreparedStatement create =  connection.prepareStatement(sql);
             PreparedStatement inserting =  connection.prepareStatement(query);)
        {
            create.executeUpdate();
            int locator = 1;
            for (java.lang.reflect.Field field:fields)
            {
                if (convertType(field.getType()).equals(""))
                {
                    continue;
                }
                field.setAccessible(true);
                if(field.getType().getTypeName().equals("boolean"))
                {
                    inserting.setBoolean(locator++,field.getBoolean(obj));
                }
                else if(field.getType().getTypeName().equals("int"))
                {
                    inserting.setInt(locator++,field.getInt(obj));
                }
                else if(field.getType().getTypeName().equals("float"))
                {
                    inserting.setFloat(locator++,field.getFloat(obj));
                }
                else if(field.getType().getTypeName().equals("double"))
                {
                    inserting.setDouble(locator++,field.getDouble(obj));
                }
                else
                {
                    Object ob = field.get(obj);
                    inserting.setString(locator++, (ob != null) ? ob.toString(): "null");
                }
            }
            val = inserting.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return val;
    }



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
                for (java.lang.reflect.Field field : fields)
                {
                    field.setAccessible(true);
                    if(field.getType().getTypeName().equals("boolean"))
                    {
                        field.setBoolean(obj, resultSet.getBoolean(location++));
                    }
                    else if(field.getType().getTypeName().equals("int"))
                    {
                        field.setInt(obj,resultSet.getInt(location++));
                    }
                    else if(field.getType().getTypeName().equals("float"))
                    {
                        field.setFloat(obj,resultSet.getFloat(location++));
                    }
                    else if(field.getType().getTypeName().equals("double"))
                    {
                        field.setDouble(obj,resultSet.getDouble(location++));
                    }
                    else
                    {
                        field.set(obj, resultSet.getString(location++));
                    }
                }
                for (Object ob : matchingValues)
                {
                    if (convertType(obj.getClass()).equals(""))
                    {
                        continue;
                    }
                    if(ob.getClass().getTypeName().equals("boolean"))
                    {
                        pstmt.setBoolean(location++, (Boolean) ob);
                    }
                    else if(ob.getClass().getTypeName().equals("int"))
                    {
                        pstmt.setInt(location++, (Integer) ob);
                    }
                    else if(ob.getClass().getTypeName().equals("float"))
                    {
                        pstmt.setFloat(location++, (Float) ob);
                    }
                    else if(ob.getClass().getTypeName().equals("double"))
                    {
                        pstmt.setDouble(location++, (Double) ob);
                    }
                    else
                    {
                        pstmt.setString(location++, (String) ob);
                    }
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
        java.lang.reflect.Field[] fields = Classy.getDeclaredFields();
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
                for (java.lang.reflect.Field field : fields)
                {
                    if (convertType(field.getType()).equals(""))
                    {
                        continue;
                    }
                    field.setAccessible(true);
                    if(field.getType().getTypeName().equals("boolean"))
                    {
                        field.setBoolean(obj,resultSet.getBoolean(location++));
                    }
                    else if(field.getType().getTypeName().equals("int"))
                    {
                        field.setInt(obj,resultSet.getInt(location++));
                    }
                    else if(field.getType().getTypeName().equals("float"))
                    {
                        field.setFloat(obj,resultSet.getFloat(location++));
                    }
                    else if(field.getType().getTypeName().equals("double"))
                    {
                        field.setDouble(obj,resultSet.getDouble(location++));
                    }
                    else
                    {
                        field.set(obj, resultSet.getString(location++));
                    }
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

    public int update(Class<?> Classy, Object[] changeValues, Field[] changeKeys,
                      Object[] matchingValues, Field[] matchingKeys) throws GrimException
    {
        int output = -1;
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
        try (Connection connection= DBConnector.getConnection();
             PreparedStatement pstmt =  connection.prepareStatement(query); )
        {
            int location = 1;
            for (Object obj : changeValues)
            {
                if (convertType(obj.getClass()).equals(""))
                {
                    continue;
                }
                if(obj.getClass().getTypeName().toString().equals("Boolean"))
                {
                    pstmt.setBoolean(location++, (Boolean) obj);
                }
                else if(obj.getClass().getTypeName().toString().equals("Int"))
                {
                    pstmt.setInt(location++, (Integer) obj);
                }
                else if(obj.getClass().getTypeName().toString().equals("Float"))
                {
                    pstmt.setFloat(location++, (Float) obj);
                }
                else if(obj.getClass().getTypeName().toString().equals("Double"))
                {
                    pstmt.setDouble(location++, (Double) obj);
                }
                else
                {
                    pstmt.setString(location++, (String) obj);
                }
            }
            for (Object obj : matchingValues)
            {
                if (convertType(obj.getClass()).equals(""))
                {
                    continue;
                }
                if(obj.getClass().getTypeName().equals("boolean"))
                {
                    pstmt.setBoolean(location++, (Boolean) obj);
                }
                else if(obj.getClass().getTypeName().equals("int"))
                {
                    pstmt.setInt(location++, (Integer) obj);
                }
                else if(obj.getClass().getTypeName().equals("float"))
                {
                    pstmt.setFloat(location++, (Float) obj);
                }
                else if(obj.getClass().getTypeName().equals("double"))
                {
                    pstmt.setDouble(location++, (Double) obj);
                }
                else
                {
                    pstmt.setString(location++, (String) obj);
                }
            }
            System.out.println(pstmt);
            output = pstmt.executeUpdate();
        }
        catch (SQLException e)
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
    public int delete(Class<?> Classy, Object[] matchingValues, Field[] matchingKeys) throws GrimException
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
        try (Connection connection= DBConnector.getConnection();
             PreparedStatement pstmt =  connection.prepareStatement(query); )
        {
            int location = 1;
            for (Object obj : matchingValues)
            {
                if (convertType(obj.getClass()).equals(""))
                {
                    continue;
                }
                if(obj.getClass().getTypeName().equals("boolean"))
                {
                    pstmt.setBoolean(location++, (Boolean) obj);
                }
                else if(obj.getClass().getTypeName().equals("int"))
                {
                    pstmt.setInt(location++, (Integer) obj);
                }
                else if(obj.getClass().getTypeName().equals("float"))
                {
                    pstmt.setFloat(location++, (Float) obj);
                }
                else if(obj.getClass().getTypeName().equals("double"))
                {
                    pstmt.setDouble(location++, (Double) obj);
                }
                else
                {
                    pstmt.setString(location++, (String) obj);
                }
            }
            val = pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return val;
    }

    /**
     *
     * @param dataType
     * @return
     */
    private static String convertType(Type dataType)
    {
        switch (dataType.getTypeName())
        {
            case "boolean":
            case "java.lang.Boolean":
                return "bool";
            case "int":
            case "java.lang.Integer":
                return "int";
            case "float":
            case "java.lang.Float":
                return "float";
            case "double":
            case "java.lang.Double":
                return "double";
        }
        if 	(dataType.getTypeName().equals("java.lang.String"))
        {
            return "text";
        }
        return "";
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