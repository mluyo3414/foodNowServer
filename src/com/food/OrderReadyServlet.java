package com.food;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author James "Modular" Dagres
 * @author Miguel "Bear" Suarez
 * @author Matt "the Boss" Luckam
 * @author Carl "Ambassador" Barbee
 * 
 * @version Nov 27, 2013
 * 
 *          This servlet receives data from the admin application and updates
 *          the database and JSON information accordingly.
 * 
 */
public class OrderReadyServlet extends HttpServlet
{
    private static Connection orderQueueConnection_;

    /*
     * (non-Javadoc)
     * 
     * This function is called when the servlet is first instanced
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException
    {
        super.init();

        // Add received order to database
        try
        {
            Class.forName( "org.sqlite.JDBC" );
            orderQueueConnection_ =
                    DriverManager.getConnection( "jdbc:sqlite:orders.db" );
        }
        catch ( Exception e )
        {
            System.err
                    .print( "Error connectino to the order database: "
                            + e );
        }
    }

    /**
     * Accessible from admin app to form the ListView.
     */
    protected void doGet( HttpServletRequest request,
            HttpServletResponse response ) throws IOException
    {
        response.setContentType( "text/plain" );
        response.getWriter().write( MainServlet.clientArray.toString() );
    }

    /**
     * accessible for the admin app. Deletes orders when receives post with
     * information.
     */
    protected void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
            IOException
    {
        response.setContentType( "JSON" );

        String username = request.getParameter( "username" );
        username = username.trim();
        String order = request.getParameter( "order" );
        order = order.trim();
        String location = request.getParameter( "location" );

        String JSONusername = "NAME";
        String JSONorder = "ORDER";
        String JSONLocation = "LOCATION";
        JSONObject newClient = new JSONObject();
        try
        {
            newClient.put( JSONusername, username );
            newClient.put( JSONorder, order );
            newClient.put( JSONLocation, location );

            String JOBJtoString = newClient.toString();

            String ClientArrayRaw = MainServlet.clientArray.toString();

            String newClientArray = ClientArrayRaw.replace( JOBJtoString, "" );
            // if object is first

            newClientArray = newClientArray.replace( "[,", "[" );
            // if object is last
            newClientArray = newClientArray.replace( ",]", "]" );
            // if object is in the middle
            newClientArray = newClientArray.replace( ",,", "," );

            JSONArray newArray = null;

            newArray = new JSONArray( newClientArray );

            // replacing old array
            MainServlet.clientArray = newArray;
        }
        catch ( JSONException e )
        {
            System.err.print( "Error creating the JSON array: " + e );
        }

        // Call the helper function to handle the completed order
        try
        {
            // TODO: change the 1 to be an id pulled
            orderMadeHelper( 1 );
        }
        catch ( SQLException e )
        {
            System.err
                    .print( "Error handling the finished order with the database: "
                            + e );
        }
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
                DataServlet.DATE_FORMAT.format( Calendar.getInstance()
                        .getTime() ) );

        // TODO: Notify user that their order is ready
    }

    /**
     * TODO for debugging purposes
     * 
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
     * Thus function is called to update the time the order is ready to pick up
     * in the database
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
     * TODO: this function is called when an admin wants the database to be
     * reset
     * 
     * @param c
     */
    private static void emptyDataBase( Connection c )
    {
        // TODO:
    }
}