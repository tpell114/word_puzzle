import java.net.*;
import java.io.*;
import java.util.ArrayList;
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
                System.out.println("Received:" + inputMsg);                        //FOR TESTING
                byte cmdCode = parseCommand(inputMsg);
                String msgContents = parseContents(inputMsg);
                handleClientReq(cmdCode, msgContents, out, clientSocket);
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

    private String parseContents(String message)
    {
        String[] parts = message.split(" ", 2);
        try
        {
            return parts[1];  
        }
        catch (NumberFormatException e)
        {
            System.out.println("Error isolating message contents" + parts[1]);
            //need to handle this error in run()
            return "ERROR ERROR";
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
           // try
           // {
              //  String[] difficulty = message.split(":",2);
              //  int numOfWords = Integer.parseInt(difficulty[0]);
              //  int factor =  Integer.parseInt(difficulty[1]);
               // out.println("Contacting word repository to setup level with " + numOfWords + "number or words" + "and a difficulty factor of" + factor); 
                handleSetupLevel();//(numOfWords, factor);
                //logic to contact word repo   
          //  }
          //  catch(Exception e)
          //  {
          //      System.out.println("Invalid level set parameters: " + message);
          //      out.println("Error: Invalid level parameters. Use format 'n:f'");
          //  }
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
                String exists;
                exists = contactWordRepository(cmdCode, message);
                out.println("Does the word " + message + " exist in the word repo? : " + exists);
                }
                catch(Exception e)
                {
                    System.out.println("Error verifying word with repo: " + message);
                    out.println("Word could not be validated please try again");
                }
            break;

            case ProtocolConstants.CMD_ADD_WORD:
            System.out.println("Request to ADD " + message + " received");
            String added = contactWordRepository(cmdCode, message);
            System.out.println("Sent " + message + " to word repo and received: " + added); 
            sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, message + " added?: " + added);
            break;

            case ProtocolConstants.CMD_REMOVE_WORD:
            System.out.println("Request to REMOVE " + message + " received");
            String removed = contactWordRepository(cmdCode, message);
            System.out.println("sent " + message + " to word repo and received: " + removed); 
            sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, message + " removed?: " + removed);
            break;

            default:
            System.out.println("received unrecognized command and message: " + cmdCode + " " + message);
            out.println("ERROR: Unrecognized command and message:" + cmdCode + " " + message);
            break;
        }
    }

    void handleClientExit(PrintStream out, Socket clientSocket)
    {
        try 
        {
        sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, "Terminating connection"); 
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
            sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, "Server: Welcome " + message + " to the Word Puzzle Game Server!");
        }
        catch(Exception e)
        {
            System.out.println("Error sending welcome message: "+ e.getMessage());
            e.printStackTrace();
        }

    }

    void handleSetupLevel()//int numOfWords, int factor)
    {
       ArrayList<String> howords = new ArrayList<>();

       howords.add("system");
       howords.add("reliable");
       howords.add("design");

       PuzzleObject testpzl = new PuzzleObject(3);
       String HI = PuzzleObject.displayPuzzle( howords, "distributed");

       System.out.println("howards" + howords);

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


    private String contactWordRepository(byte cmdCode, String message)
    {
        try(DatagramSocket socket = new DatagramSocket())
        {
            InetAddress address = InetAddress.getByName("localhost");
        
            String fullMessage = String.format("%02X %s%s", cmdCode, message, ProtocolConstants.MSG_TERMINATOR);
            byte[] buffer = fullMessage.getBytes();

            DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, 9090);
            socket.send(request);

            byte[] responseBuffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(response);

            return new String(response.getData(), 0, response.getLength()).trim();
        }
        catch (IOException e) 
        {
            System.err.println("Error communicating with WordRepoMicroservice: " + e.getMessage());
            return "ERROR"; 
        }

    }
    }




