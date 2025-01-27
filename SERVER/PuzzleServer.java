import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PuzzleServer
{
    private ServerSocket serverSocket;

    public PuzzleServer(int port)
    {
        try
        {
        serverSocket = new ServerSocket(port);
        }
        catch (IOException e)
        {
            System.out.println("Exception caught when trying to bind to port "
            + port);
            System.out.println(e.getMessage());  
        } 
        

    } 

	public static void main(String[] args)
    {
        int port = 8080;
        PuzzleServer server = null;
        try
        {
            server = new PuzzleServer(port);
            server.listen();
        }
        catch (Exception e)
        {
            System.out.println("Exception caught when trying to establish server with port "
            + port);
            System.out.println(e.getMessage());  
        }
    }

    public void listen() 
    {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        while (true) 
        {
            try 
            {
                System.out.println("Listening for incoming requests...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");

                threadPool.submit(new WorkerThread(clientSocket));
            }

            catch (IOException e) 
            {
                System.out.println("Error accepting client connection: " + e.getMessage());
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
    }

    class WorkerThread implements Runnable
    {
        private Socket clientSocket;
        public WorkerThread(Socket clientSocket)
        {
            this.clientSocket = clientSocket;
        }
    
    @Override
    public void run()
    {
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintStream out = new PrintStream(clientSocket.getOutputStream());

            String inputMsg;
            while ((inputMsg = in.readLine()) != null) 
            {
                System.out.println("Received:" + inputMsg); //FOR TESTING
                byte cmdCode = parseCommand(inputMsg);
                handleClientReq(cmdCode, inputMsg, out, clientSocket);
            }
        
        clientSocket.close();
        System.out.println("Client connection closed.");
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
            return -1; //need to handle this error in run()
        }
    }

    private void handleClientReq(byte cmdCode, String message, PrintStream out, Socket clientSocket)
    {
        switch (cmdCode)
        {
            case ProtocolConstants.CMD_EXIT:
            handleClientExit(out, clientSocket);
            break;

            case ProtocolConstants.CMD_SIGN_IN:
            handleClientSignIn(out, message);
            break;

            case ProtocolConstants.CMD_LEVEL_SET:
            try
            {
                String[] difficulty = message.split(":");
                int numOfWords = Integer.parseInt(difficulty[0]);
                int factor =  Integer.parseInt(difficulty[1]);
                out.println("Contacting word repository to setup level with " + numOfWords + "number or words" + "and a difficulty factor of" + factor); \
                handleSetupLevel(numOfWords, factor);
                //logic to contact word repo   
            }
            catch(Exception e)
            {
                System.out.println("Invalid level set parameters: " + message);
                out.println("Error: Invalid level parameters. Use format 'n:f'");
            }
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
            
            try{
                boolean exists = false;
                exists = checkIfWordExists(message);
                out.println("Does the word " + message + " exist in the word repo? : " + exists);
                }
                catch(Exception e)
                {
                    System.out.println("Error verifying word with repo: " + message);
                    out.println("Word could not be validated please try again");
                }
            // logic to contact word repo and verify existence of word
            break;

            case ProtocolConstants.CMD_ADD_WORD:
            addWordToRepo(message);
            break;

            case ProtocolConstants.CMD_REMOVE_WORD:
            removeWordFromRepo(message);
            break;

            default:
            System.out.println("Recieved unrecognized command and message: " + cmdCode + " " + message);
            out.println("ERROR: Unrecognized command and message:" + cmdCode + " " + message);
            break;
        }
    }

    void handleClientExit(PrintStream out, Socket clientSocket)
    {
        try 
        {
        sendMessage(out, ProtocolConstants.CMD_SND_Mischellaneous, "Terminating connection"); 
           clientSocket.close();
           System.out.println("Client connection closed");
        }
        catch(IOException e)
        {
            System.out.println("Error while closing client resources: "+ e.getMessage());
            e.printStackTrace();
        }
    }

    void handleClientSignIn(PrintStream out, String message)
    {
        try
        {
            sendMessage(out, ProtocolConstants.CMD_SND_WELCOME, "Welcome " + message + "to the Word Puzzle Game Server!");
        }
        catch(Exception e)
        {
            System.out.println("Error sending welcome message: "+ e.getMessage());
            e.printStackTrace();
        }

    }

    void handleSetupLevel(int numOfWords, int factor)
    {

    }

    void handleClientGuess()
    {

    }

    void handleScoreCheck()
    {

    }

    void handleNewGameReq()
    {

    }

    void handleAbortGame()
    {

    }

    void checkIfWordExists(String message)
    {

    }

    void  addWordToRepo(String message)
    {

    }

    void removeWordFromRepo(String message)
    {

    }

    private void sendMessage(PrintStream out, byte cmdCode, String message)
    {
        try
        {
            String fullMessage = String.format("%02X %s%s", cmdCode, message, ProtocolConstants.MSG_TERMINATOR);
            out.print(fullMessage);
            out.flush();
        }
        catch(Exception e)
        {
            System.out.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }




}