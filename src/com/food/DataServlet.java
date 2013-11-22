package com.food;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import edu.vt.ece4564.example.Student;

/**
 * 
 * @author Miguel "The Bear" Suarez
 * @author James "Modular" Dagres
 * @author Carl "CAH" Barbee
 * @author Matt "Sharknado" Luckam
 * 
 *         Adds data to the server from the client, creates two databases to
 *         store the orders
 * 
 */
public class DataServlet extends Servlet
{
    // TODO: implement a static ID for every order

    // An instance of SimpleDateFormat used for formatting
    // the string representation of date (month/day/year)
    private final static DateFormat DATE_FORMAT = new SimpleDateFormat(
            "MM/dd/yyyy HH:mm:ss" );

    public DataServlet()
    {

        createOrderQueueDatabase();
    }

    /**
     * Adds new orders to the server from the client app.
     */
    protected void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
            IOException
    {
        response.setContentType( "JSON" );
        String param = request.getParameter( "myparam" );
        response.getWriter().write( "Hello from Data Base" );
        // creating JSON object
        String username = request.getParameter( "username" );
        username = username.trim();
        String order = request.getParameter( "order" );
        order = order.trim();
        String location = request.getParameter( "location" );
        // passing elements
        String JSONusername = "NAME";
        String JSONorder = "ORDER";
        String JSONLocation = "LOCATION";
        JSONObject newClient = new JSONObject();
        try
        {
            newClient.put( JSONusername, username );
            newClient.put( JSONorder, order );
            newClient.put( JSONLocation, location );
        }
        catch ( JSONException e )
        {
            System.err.print( "Error in the data servlet: " + e );
        }

        clientArray.put( newClient );
        response.getWriter().write( clientArray.toString() );

        // Add the new order to the orderqueue database
        addOrderToQueueDatabase( "Jimmy", "BIG BURRITO", "$6.90",
                false,
                DATE_FORMAT.format( Calendar.getInstance().getTime() ) );

        // Grab a hold of the student after modifying it
        printOrderQueue( orderQueueConnection_ );
    }

    /**
     * Initializes the order queue database
     */
    private createOrderQueueDatabase()
    {
        Class.forName( "org.sqlite.JDBC" );
        orderQueueConnection_ =
                DriverManager.getConnection( "jdbc:sqlite:orderQueue.db" );
        System.out.println( "Openeddatabase" );

        // TODO: the following if is a hack, figure out how to check to check
        // and see if the database is already made
        if ( null == orderQueueConnection_ )
        {
            // Create the database table and add the students
            createOrderQueueTable( orderQueueConnection_ );
        }
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
                " Payed         BOOLEAN     NOT NULL, " +
                " Time_Of_Order          TEXT       NOT NULL, " +
                " Time_Ready    TEXT)";
        stmt.executeUpdate( sql );
        stmt.close();
        System.out.println( "Createdtable" );
    }

    /**
     * This function adds an order to the orderQueue Database
     * 
     * @param name
     * @param orderSummary
     * @param orderCost
     * @param paid
     * @param timeOfOrderPlaced
     * @throws SQLException
     */
    private static void addOrderToQueueDatabase( String name,
            String orderSummary,
            String orderCost, Boolean paid,
            String timeOfOrderPlaced ) throws SQLException
    {
        Statement stmt = orderQueueConnection_.createStatement();
        String sql =
                "INSERT INTO class (id,Name,Contents_Of_Order,Cost,Payed,Time_Of_Order "
                        + "VALUES ("
                        + Integer.toString( orderIdNumber_ )
                        + ", '"
                        + name
                        + "', '"
                        + orderSummary
                        + "', '"
                        + orderCost
                        + "', '"
                        + paid.toString()
                        + "', '"
                        + timeOfOrderPlaced
                        + "');";
        stmt.executeUpdate( sql );
        stmt.close();
    }

    /**
     * Pulls the students from the database
     * 
     * @param c
     * @throws SQLException
     */
    public static void printOrderQueue( Connection c ) throws SQLException
    {
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery( "SELECT * FROM class;" );
        while ( rs.next() )
        {
            int id = rs.getInt( "id" );
            String name = rs.getString( "Name" );
            String order = rs.getString( "Contents_Of_Order" );
            String orderTime = rs.getString( "Time_Of_Order" );
            String cost = rs.getString( "Cost" );
            Boolean payed = rs.getBoolean( "Payed" );
            String timeCompleted = rs.getString( "Time_Ready" );

            // Display the information pulled
            System.out.println( "Got Name: " + name );
            System.out.println( "   Contents_Of_Order: " + order );
            System.out.println( "   Cost: " + cost );
            System.out.println( "   Payed: " + payed );
            System.out.println( "   Time_Of_Order: " + orderTime );
            System.out.println( "   Time_Ready: " + timeCompleted );
            System.out.println( "   id: " + id );

        }
        rs.close();
        stmt.close();
    }

    /**
     * @param c
     * @param orderId
     * @throws SQLException
     */
    public static void
            removeOrderFromDatabaseQueue( Connection c, int orderId )
                    throws SQLException
    {
        // Prepared statements secures the database and avoids sequel injection
        PreparedStatement stmt =
                c.prepareStatement( "DELETE FROM class WHERE id=?" );
        stmt.setInt( 1, orderId );

        // Only use executeUpdate(); when making changes to the server:
        stmt.executeUpdate();

        stmt.close();
    }

    /**
     * Thus function is called to update the name on a customers order
     * 
     * @param c
     * @param id
     * @param newName
     * @throws SQLException
     */
    public static void
            updateOrdersName( Connection c, int id, String newName )
                    throws SQLException
    {
        PreparedStatement stmt =
                c.prepareStatement( "UPDATE class SET Name=? WHERE id=?" );
        stmt.setString( 1, newName );
        stmt.setInt( 2, id );

        // Execute update when updating something in the database
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Thus function is called to update the time ready to pick up column
     * 
     * @param c
     * @param id
     * @param timeOrderWasReady
     * @throws SQLException
     */
    public static void
            updateOrderQueueStatus( Connection c, int id,
                    String timeOrderWasReady )
                    throws SQLException
    {
        PreparedStatement stmt =
                c.prepareStatement( "UPDATE class SET Time_Ready=? WHERE id=?" );
        stmt.setString( 1, timeOrderWasReady );
        stmt.setInt( 2, id );

        // Execute update when updating something in the database
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Stores an object into the specified database. Note: The passed Object "O"
     * must implement java.io.Serializable for this code to work…
     * 
     * @param c
     * @param o
     *            Must implement java.io.Serializable
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void storeObjectIntoDatabase( Connection c, Object o )
            throws SQLException, IOException, ClassNotFoundException
    {
        String sql =
                "CREATE TABLE IF NOT EXISTS objects (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, data BLOB NOT NULL);";
        Statement stmt = c.createStatement();
        stmt.executeUpdate( sql );
        // Convert object to byte[]
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        (new ObjectOutputStream( bstream )).writeObject( o );
        byte[] storable = bstream.toByteArray();
        // Store in database
        PreparedStatement insert =
                c.prepareStatement( "INSERT INTO objects (data) VALUES (?)" );
        insert.setBytes( 1, storable );
        insert.executeUpdate();
        // Extract from database
        Statement getData = c.createStatement();
        ResultSet rs =
                getData.executeQuery( "SELECT data FROM objects WHERE id=1;" );
        byte[] retrieved = rs.getBytes( "data" );
        // Convert back to object (e.g "inflate")
        ByteArrayInputStream bin = new ByteArrayInputStream( retrieved );
        ObjectInputStream ois = new ObjectInputStream( bin );
        @SuppressWarnings( "unused" )
        Object originalObject = ois.readObject();
    }

    /**
     * This function is called when the food for an order is ready to be picked
     * up. It handles the event and updates the appropriate databases and Alerts
     * the corresponding client that their order is ready.
     * 
     * @throws SQLException
     */
    private static void orderMadeHelper( int id ) throws SQLException
    {
        updateOrderQueueStatus( orderQueueConnection_, id,
                DATE_FORMAT.format( Calendar.getInstance().getTime() ) );

        // TODO: Notify user that their order is ready
    }

    /**
     * This function is called when a customer pays and picks up an order, it
     * removes it from the orderQueue database and adds it to the transaction
     * database
     * 
     * @throws SQLException
     */
    private static void orderPickedUpHelper( int id ) throws SQLException
    {
        // TODO: Check to see if they paid before the order was picked up, if
        // they paid when they picked up the order then log the payment
        // information.

        // TODO: Add the order to the admin database.

        removeOrderFromDatabaseQueue( orderQueueConnection_, id );

    }
}