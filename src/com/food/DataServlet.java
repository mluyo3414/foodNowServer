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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

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
    final static DateFormat DATE_FORMAT = new SimpleDateFormat(
            "MM-dd-yyyy HH:mm:ss" );

    private static Connection orderQueueConnection_;

   
    private static Integer orderIdNumber_ = 0;
    
    private static String phone_="";

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
        order = order.replace( "[", "" );
        order = order.replace( "]", "" );

        String time = request.getParameter( "time" );
        String total = request.getParameter( "total" );
         phone_ = request.getParameter( "phone" );
         String paymentMethod = request.getParameter( "paymentmethod" );

        // passing elements
        String JSONusername = "NAME";
        String JSONorder = "ORDER";
        String JSONLocation = "TIME";
        String JSONTotal = "TOTAL";
        String JSONPhone = "PHONE";
        String JSONConfirmation = "CONFIRMATION";
        orderIdNumber_ =
                1000 + (int) (Math.random() * ((9999 - 1000) + 1));
        //add to total 
        MainServlet.totalSales = MainServlet.totalSales + Double.parseDouble( total );
        DecimalFormat twoDForm = new DecimalFormat( "#.##" ); 
        MainServlet.totalSales = Double.valueOf( twoDForm.format( MainServlet.totalSales ) );
        String totalSalesString = MainServlet.totalSales.toString();
        
        
        String orderString = Integer.toString( orderIdNumber_ );
        JSONObject newClient = new JSONObject();
        try
        {
        	newClient.put(JSONConfirmation,orderString);
        	newClient.put(JSONPhone,phone_);
        	newClient.put( JSONusername, username );
            newClient.put( JSONorder, order );
            newClient.put( JSONLocation, time );
            newClient.put( JSONTotal, total );
        }
        catch ( JSONException e )
        {
            System.err.print( "Error in the data servlet: " + e );
        }


        response.getWriter().write( orderIdNumber_.toString() );
 
        MainServlet.clientArray.put( newClient );

        try
        {

            Class.forName( "org.sqlite.JDBC" );
            orderQueueConnection_ =
                    DriverManager.getConnection( "jdbc:sqlite:orders.db" );

            addOrderToQueueDatabase( username, order, total, paymentMethod, time,
                    phone_, totalSalesString );

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
            String orderSummary, String orderCost, String paid,
            String timeOfOrderPlaced, String phoneNumber, String totalSales ) throws SQLException
    {
        String sql =
                "INSERT INTO class (id,Name,Contents_Of_Order,Cost,Paid,Time_Of_Order,Total_Sales,Phone_Number) "
                        + "VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement stmt = orderQueueConnection_.prepareStatement( sql );
        stmt.setString( 1, Integer.toString( orderIdNumber_ ) );
        stmt.setString( 2, name );
        stmt.setString( 3, orderSummary );
        stmt.setString( 4, orderCost );
        stmt.setString( 5, paid.toString() );
        stmt.setString( 6, timeOfOrderPlaced );
        stmt.setString( 7, totalSales );
        stmt.setString( 8, phoneNumber );
        

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


}