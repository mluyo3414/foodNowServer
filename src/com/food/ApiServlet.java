package com.food;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiServlet extends Servlet
{
/**
 *   Accessible from admin app to form the ListView.
 */
	protected void doGet( HttpServletRequest request,
			HttpServletResponse response ) throws IOException
	{
		response.setContentType( "text/plain" );
		response.getWriter().write( clientArray.toString() );
	}
	/**
	 *  accessible for the admin app. Deletes orders when receives post with information.
	 */
	protected void doPost( HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException
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
		} catch ( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String JOBJtoString = newClient.toString();

		String ClientArrayRaw = clientArray.toString();

		String newClientArray = ClientArrayRaw.replace( JOBJtoString, "" );
		// if object is first

		newClientArray = newClientArray.replace( "[,", "[" );
		//if object is last
		newClientArray = newClientArray.replace( ",]", "]" );
		//if object is in the middle
		newClientArray = newClientArray.replace( ",,", "," );
		
		JSONArray newArray = null;
		try
		{
			 newArray = new JSONArray(newClientArray);
		} catch ( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//replacing old array
		clientArray=newArray;
		
		
		
		
		

	}
}
