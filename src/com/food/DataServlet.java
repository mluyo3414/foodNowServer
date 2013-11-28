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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

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
public class DataServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    // An instance of SimpleDateFormat used for formatting
    // the string representation of date (month/day/year)
    private final static DateFormat DATE_FORMAT = new SimpleDateFormat(
            "MM/dd/yyyy HH:mm:ss" );

    private static Connection orderQueueConnection_;
    private static Connection adminTrackerConnection_;

    // TODO: Implement, also in main
    private static Integer orderIdNumber_ = 0;

    /**
     * Adds new orders to the server from the client app.
     */
    protected void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
            IOException
    {
        response.setContentType( "text/plain" );

        // creating JSON object
        String username = request.getParameter( "username" );
        username = username.trim();
        String order = request.getParameter( "order" );
        order = order.trim();
        
        String time = request.getParameter( "location" );
        String total = request.getParameter( "total" );

        // passing elements
        String JSONusername = "NAME";
        String JSONorder = "ORDER";
        String JSONLocation = "LOCATION";
        String JSONTotal = "TOTAL";
        JSONObject newClient = new JSONObject();
        try
        {
            newClient.put( JSONusername, username );
            newClient.put( JSONorder, order );
            newClient.put( JSONLocation, time );
            newClient.put( JSONTotal, total );
        }
        catch ( JSONException e )
        {
            System.err.print( "Error in the data servlet: " + e );
        }

        orderIdNumber_ =
                1000 + (int) (Math.random() * ((9999 - 1000) + 1));
        response.getWriter().write( orderIdNumber_.toString() );
        MainServlet.clientArray.put( newClient );

        // TODO: we need to get paid boolean and order total from client
        // Add received order to database
        try
        {

            Class.forName( "org.sqlite.JDBC" );
            orderQueueConnection_ =
                    DriverManager.getConnection( "jdbc:sqlite:orderqueue.db" );
            adminTrackerConnection_ =
                    DriverManager
                            .getConnection( "jdbc:sqlite:admintracker.db" );

            // TODO: change the hard coded phone# to come from the client app
            addOrderToQueueDatabase( username, order, total, false, time,
                    "757 636 3087" );

            // Grab a hold of the order after modifying it
            printOrderQueue( orderQueueConnection_ );
        }
        catch ( Exception e )
        {
            System.err
                    .print( "Error adding/printing new order to order queue database: "
                            + e );
        }
    }

    /**
     * This function adds an order to the orderQueue Database
     * 
     * @param name
     * @param orderSummary
     * @param orderCost
     * @param paid
     * @param timeOfOrderPlaced
     * @param phoneNumber
     * @throws SQLException
     */
    private static void addOrderToQueueDatabase( String name,
            String orderSummary, String orderCost, Boolean paid,
            String timeOfOrderPlaced, String phoneNumber ) throws SQLException
    {
        String sql =
                "INSERT INTO class (id,Name,Contents_Of_Order,Cost,Paid,Time_Of_Order,Phone_Number) "
                        + "VALUES (?,?,?,?,?,?,?)";
        PreparedStatement stmt = orderQueueConnection_.prepareStatement( sql );
        stmt.setString( 1, Integer.toString( orderIdNumber_ ) );
        stmt.setString( 2, name );
        stmt.setString( 3, orderSummary );
        stmt.setString( 4, orderCost );
        stmt.setString( 5, paid.toString() );
        stmt.setString( 6, timeOfOrderPlaced );
        stmt.setString( 7, phoneNumber );

        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Once an order has been paid for and picked up this function is called to
     * add it to the admin tracker database
     * 
     * @param Id
     * @param orderSummary
     * @param orderCost
     * @param timeOfOrderPlaced
     * @param timeToCreateOrder
     * @param timeOfOrderPickup
     * @throws SQLException
     */
    private static void addOrderToAdminTrackerDatabase( String Id,
            String orderSummary,
            String orderCost,
            String timeOfOrderPlaced, String timeToCreateOrder,
            String timeOfOrderPickup )
            throws SQLException
    {
        String sql =
                "INSERT INTO class (id,Name,Contents_Of_Order,Cost,Paid,Time_Of_Order "
                        + "VALUES (?,?,?,?,?,?)";
        PreparedStatement stmt = adminTrackerConnection_.prepareStatement( sql );

        stmt.setString( 1, Id );
        stmt.setString( 2, orderSummary );
        stmt.setString( 3, orderCost );
        stmt.setString( 4, timeOfOrderPlaced );
        stmt.setString( 5, timeToCreateOrder );
        stmt.setString( 6, timeOfOrderPickup );
        stmt.executeUpdate();
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
            Boolean paid = rs.getBoolean( "Paid" );
            String timeCompleted = rs.getString( "Time_Ready" );
            String PhoneNumber = rs.getString( "Phone_Number" );

            // Display the information pulled
            System.out.println( "Got Name: " + name );
            System.out.println( " PhoneNumber: " + PhoneNumber );
            System.out.println( "   Contents_Of_Order: " + order );
            System.out.println( "   Cost: " + cost );
            System.out.println( "   Paid: " + paid );
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
    public static void updateOrdersName( Connection c, int id, String newName )
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
    public static void updateOrderQueueStatus( Connection c, int id,
            String timeOrderWasReady ) throws SQLException
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
        // TODO: Needs to be tested

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
     * @throws ParseException
     */
    private static void orderPickedUpHelper( int id ) throws SQLException,
            ParseException
    {
        // TODO: Needs to be tested
        Statement stmt = orderQueueConnection_.createStatement();
        ResultSet rs = stmt.executeQuery( "SELECT value FROM class;" );

        String cost = rs.getString( "Cost" );
        String order = rs.getString( "Contents_Of_Order" );
        String timeOfOrderPlacedString = rs.getString( "Time_Of_Order" );
        String timeReadyForPickupString = rs.getString( "Time_Ready" );

        Date timeOfOrderPlaced =
                new SimpleDateFormat( DATE_FORMAT.toString() )
                        .parse( timeOfOrderPlacedString );

        Date timeReadyForPickup =
                new SimpleDateFormat( DATE_FORMAT.toString() )
                        .parse( timeReadyForPickupString );

        long differenceInMillis =
                timeReadyForPickup.getTime()
                        - timeOfOrderPlaced.getTime();

        // Add the order to the admin tracker database and remove it from the
        // order queue
        addOrderToAdminTrackerDatabase( Integer.toString( id ),
                order, cost, timeOfOrderPlacedString,
                DATE_FORMAT.format( differenceInMillis ),
                DATE_FORMAT.format( Calendar.getInstance().getTime() ) );

        removeOrderFromDatabaseQueue( orderQueueConnection_, id );
    }
}