package com.food;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.json.JSONArray;

/**
 * 
 * @author Miguel S
 * 
 *         This is the main part of the server
 * 
 */
public class MainServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    protected static JSONArray clientArray;

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

}
