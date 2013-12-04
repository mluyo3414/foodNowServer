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
                    .print( "Error connecting to the order database: "
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
        //pulling parameters
    	response.setContentType( "text/plain" );
        String username = request.getParameter( "username" );
        username = username.trim();
        String order = request.getParameter( "order" );
        order = order.trim();
        String time = request.getParameter( "time" );
        String total = request.getParameter( "total" );
        String phone = request.getParameter( "phone" );
        String confirmation = request.getParameter( "confirmation" );

        String JSONusername = "NAME";
        String JSONorder = "ORDER";
        String JSONLocation = "TIME";
        String JSONTotal = "TOTAL";
        String JSONPhone = "PHONE";
        String JSONConfirmation = "CONFIRMATION";
        //creating new JSONObject to delete from array
        JSONObject newClient = new JSONObject();
        try
        {
        	newClient.put(JSONConfirmation,confirmation);

        	newClient.put(JSONPhone,phone);
        	newClient.put( JSONusername, username );
            newClient.put( JSONorder, order );
            newClient.put( JSONLocation, time );
            newClient.put( JSONTotal, total );

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
            orderMadeHelper( Integer.parseInt( confirmation ) );
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
    
    
    }


    /**
     * Thus function is called to update the time the order is ready to pick up
     * in the database. It is triggered by the admin app.
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
    

}