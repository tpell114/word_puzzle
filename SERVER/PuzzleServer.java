import java.net.*;
import java.io.*;

public class PuzzleServer
{
    private ServerSocket serverSocket;

    public PuzzleServer(int port)
    {
        try
        {
        serverSocket = new ServerSocket(port);
        }
        catch (SocketException e)
        {
            System.out.println("Exception caught when trying to bind to port "
            + port);
            System.out.println(e.getMessage());  
        } 
        }

    } 

	public static void main(String[] args)
    {
        int port = 0;
        PuzzleServer server = null;
        try
        {
            port = 8080;
            server = new PuzzleServer(port);
        }
        catch (IOException e)
        {
            System.out.println("Exception caught when trying to listen on port "
            + port + " or listening for a connection");
            System.out.println(e.getMessage());  
        }
        server.serve();
    }

    public void serve() 
    {
        while (true) 
        {
            try 
            {
                System.out.println("Listening for incoming requests...");
                Socket clientSocket = serverSocket.accept();
            } 
            catch (SocketException e) 
            {
                System.out.println(e.getMessage());
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
    }
}