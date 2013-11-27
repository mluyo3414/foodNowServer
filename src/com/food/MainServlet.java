package com.food;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.json.JSONArray;

/**
 * 
 * @author Miguel Suarez
 * @author James Dagres
 * @author Carl Barbee
 * @author Matt Luckam
 * 
 *         This is the main part of the server
 * 
 */
public class MainServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    // A singular static reference to the JSONarray containing the data
    public static JSONArray clientArray;

    // TODO: Implement, also in data servlet
    private static int orderIdNumber_ = 0;

    /**
     * 
     * @param args
     * @throws Exception
     *             Starts the server
     */
    public static void main( String[] args ) throws Exception
    {
        // port number of the server
        Server server = new Server( 8080 );
        
        // creating JSON array
        clientArray = new JSONArray();
        WebAppContext context = new WebAppContext();
        context.setWar( "war" );
        context.setContextPath( "/" );
        // starting new server
        server.setHandler( context );

        // Create the databases
        try
        {
            createOrderQueueDatabase();
            createAdminTrackerDatabase();
        }
        catch ( Exception ex )
        {
            System.err.print( "Error creating databases: " + ex );
        }

        server.start();
        server.join();
    }

    /**
     * Displays number of clients accessed from the client app.
     */
    protected void doGet( HttpServletRequest request,
            HttpServletResponse response ) throws IOException
    {
        response.setContentType( "text/plain" );
        response.getWriter().write(
                "Hello from the Server, you are now connected!\n" );
        response.getWriter().write(
                "There are currently " + clientArray.length()
                        + " order(s) in our system." );
    }

    /**
     * Initializes the order queue database.
     * 
     * @throws Exception
     */
    private static void createOrderQueueDatabase() throws Exception
    {
        Class.forName( "org.sqlite.JDBC" );

        Connection orderQueueConnection =
                DriverManager.getConnection( "jdbc:sqlite:orderQueue.db" );
        System.out.println( "Opened orderqueue database" );

        // TODO: the following if is a hack, figure out how to
        // http://stackoverflow.com/questions/3386667/query-if-android-database-exists
        // Check to see if the database is already made

        // Create the database table
        createOrderQueueTable( orderQueueConnection );
    }

    /**
     * This function creates an sql table using JDBC
     * 
     * @param c
     * @throws SQLException
     */
    public static void createOrderQueueTable( Connection c )
            throws SQLException
    {
        Statement stmt = c.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS class " +
                "(id INT PRIMARY KEY        NOT NULL, " +
                " Name           TEXT       NOT NULL, " +
                " Contents_Of_Order         TEXT       NOT NULL, " +
                " Cost          TEXT        NOT NULL, " +
                " Paid         BOOLEAN     NOT NULL, " +
                " Time_Of_Order          TEXT       NOT NULL, " +
                " Time_Ready    TEXT)";
        stmt.executeUpdate( sql );
        stmt.close();
        System.out.println( "Created orderqueue table" );
    }

    /**
     * Creates the admin tracker database file and calls the function to create
     * it's table
     * 
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private static void createAdminTrackerDatabase()
            throws ClassNotFoundException, SQLException
    {
        Class.forName( "org.sqlite.JDBC" );

        Connection orderQueueConnection =
                DriverManager.getConnection( "jdbc:sqlite:adminTracker.db" );
        System.out.println( "Opened admin tracker database" );

        // TODO: Check to see if a database has already been created.
        createAdminTrackerTable( orderQueueConnection );
    }

    /**
     * Creates the columns for that Admin tracker database
     * 
     * @throws SQLException
     */
    private static void createAdminTrackerTable( Connection c )
            throws SQLException
    {
        // TODO
        Statement stmt = c.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS class " +
                "(id INT PRIMARY KEY        NOT NULL, " +
                " Name           TEXT       NOT NULL, " +
                " Contents_Of_Order         TEXT       NOT NULL, " +
                " Cost          TEXT        NOT NULL, " +
                " Paid         BOOLEAN     NOT NULL, " +
                " Time_Of_Order          TEXT       NOT NULL, " +
                " Time_Ready    TEXT)";
        stmt.executeUpdate( sql );
        stmt.close();
        System.out.println( "Created orderqueue table" );
    }
}