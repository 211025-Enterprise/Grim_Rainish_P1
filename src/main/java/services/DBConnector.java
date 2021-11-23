package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 */

public class DBConnector
{
    private static final String url = "jdbc:postgresql://enterprise.c7a8tmbpxqmk.us-east-2.rds.amazonaws.com:5432/postgres?currentSchema=p1";
    private static final String user = "postgres";
    private static final String password = "godofrain";

    private static  DBConnector instance;
    private static Connection connect;

    public DBConnector()
    {

    }

    public static DBConnector getInstance()
    {
        if (connect == null)
        {
            instance = new DBConnector();
        }
        return instance;
    }

    //connecting to the Dbeaver SQL
    public static Connection getConnection() throws SQLException
    {
        if (instance == null || connect.isClosed())
        {
            try
            {
                Class.forName("org.postgresql.Driver");

                connect = DriverManager.getConnection(url, user, password);
            }
            catch (ClassNotFoundException | SQLException e)
            {
                e.printStackTrace();
            }
        }
        return connect;

    }
}
