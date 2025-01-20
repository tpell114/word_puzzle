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
                System.out.println("Client connected.");

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintStream out = new PrintStream(clientSocket.getOutputStream());

                String inputMsg;
                while((inputMsg = in.readLine()) != null)
                {
                    System.out.println("Received:" + inputMsg);
                    byte cmdCode = parseCommand(inputMsg);
                    handleClientReq(cmdCode, inputMsg, out);
                }

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


    private byte parseCommand(String message)
    {
        String[] parts = message.split(" ", 2);
        try
        {
            return (byte) Integer.parseInt(parts[0], 16);  
        }
        catch (NumberFormatException e)
        {
            System.out.println("Invalid cmd format" + parts[0]);
            return -1;
        }
    }

    private void handleClientReq(byte cmdCode, String message, PrintStream out)
    {
        switch (cmdCode)
        {
            case ProtocolConstants.CMD_EXIT:
            //
            break;

            case ProtocolConstants.CMD_SIGN_IN:
            //
            break;

            case ProtocolConstants.CMD_LEVEL_SET:
            //
            break;

            case ProtocolConstants.CMD_SUBMIT_GUESS:
            //
            break;
            
            case ProtocolConstants.CMD_CHECK_SCORE:
            //
            break;

            case ProtocolConstants.CMD_REQ_NEW_GAME:
            //
            break;

            case ProtocolConstants.CMD_ABORT_GAME:
            //
            break;

            case ProtocolConstants.CMD_CHECK_IF_WORD_EXISTS:
            //
            break;

            case ProtocolConstants.CMD_ADD_WORD:
            //
            break;

            case ProtocolConstants.CMD_REMOVE_WORD:
            //
            break;

            default:
            //send unknown command recieved message
            break;

        
        }
    }
}