package com.food;

import java.io.IOException;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
/**
 * 
 * @author Miguel S
 * 
 * Adding data to the server from the client
 *
 */
public class DataServlet extends MainServlet
{


	/**
	 * Adds new orders to the server from the client app.
	 */
	protected void doPost( HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException
	{

		response.setContentType( "text/plain" );
		String param = request.getParameter( "myparam" );
		response.getWriter().write( "Hello from Data Base" );
		//creating JSON object
		String username = request.getParameter( "username" );
		username = username.trim();
		String order = request.getParameter( "order" );
		order = order.trim();
		String location = request.getParameter( "location" );
		//passing elements
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		clientArray.put( newClient );
		response.getWriter().write( clientArray.toString() );

	}
}
