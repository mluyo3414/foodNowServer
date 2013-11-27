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
	// TODO: implement a static ID for every order

	// An instance of SimpleDateFormat used for formatting
	// the string representation of date (month/day/year)
	private final static DateFormat DATE_FORMAT = new SimpleDateFormat(
			"MM/dd/yyyy HH:mm:ss" );

	private static Connection orderQueueConnection_;

	// TODO: Implement, also in main
	private static int orderIdNumber_ = 0;

	/**
	 * Adds new orders to the server from the client app.
	 */
	protected void doPost( HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/plain" );
		// response.getWriter().write( "Hello from Data Base" );

		// creating JSON object
		String username = request.getParameter( "username" );
		username = username.trim();
		String order = request.getParameter( "order" );
		order = order.trim();
		// TODO: location is really time
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
		} catch ( JSONException e )
		{
			System.err.print( "Error in the data servlet: " + e );
		}

		Integer randomNumber =
				1000 + (int) (Math.random() * ((9999 - 1000) + 1));
		response.getWriter().write( randomNumber.toString() );

		// TODO: we need to get paid boolean and order total from client
		// Add received order to database, and remove test:
		try
		{

			Class.forName( "org.sqlite.JDBC" );
			orderQueueConnection_ =
					DriverManager.getConnection( "jdbc:sqlite:orderQueue.db" );

			addOrderToQueueDatabase( username, order, "$6.90", false, location );

			// TODO: add received order to database, and remove test:
			// addOrderToQueueDatabase( "Jimmy", "BIG BURRITO", "$6.90",
			// false,
			// DATE_FORMAT.format( Calendar.getInstance().getTime() ) );

			// Grab a hold of the order after modifying it
//			printOrderQueue( orderQueueConnection_ );
		} catch ( Exception e )
		{
			System.err
					.print( "Error adding/printing new order to order queue database: "
							+ e );
		}
	}

	/**
	 * Once an order has been paid for and picked up this function is called to
	 * add it to the admin tracker database
	 */
	private void addOrderToAdminTrackerDatabase( Connection c )
	{
		// TODO:
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
			String orderSummary, String orderCost, Boolean paid,
			String timeOfOrderPlaced ) throws SQLException
	{
		Statement stmt = orderQueueConnection_.createStatement();
		String sql =
				"INSERT INTO class (id,Name,Contents_Of_Order,Cost,Paid,Time_Of_Order "
						+ "VALUES (" + Integer.toString( orderIdNumber_ )
						+ ", '" + name + "', '" + orderSummary + "', '"
						+ orderCost + "', '" + paid.toString() + "', '"
						+ timeOfOrderPlaced + "');";
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
			Boolean paid = rs.getBoolean( "Paid" );
			String timeCompleted = rs.getString( "Time_Ready" );

			// Display the information pulled
			System.out.println( "Got Name: " + name );
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
	public static void removeOrderFromDatabaseQueue( Connection c, int orderId )
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