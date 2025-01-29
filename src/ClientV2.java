import java.io.*;
import java.net.Socket;

public class ClientV2 {

    private Socket clientSocket;
    private PrintStream toServer;
    private BufferedReader fromServer;

    public ClientV2() {
        try {
            clientSocket = new Socket("localhost", 8080);
            toServer = new PrintStream(clientSocket.getOutputStream());
            fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void main(String[] args) throws Exception {
        
        try {
            ClientV2 client = new ClientV2();

            System.out.println("\nWelcome to Word Puzzle!\n"
                             + "=======================\n"
                             + "\nPlease enter your name:");

            String name = System.console().readLine().trim();
            client.sendToServer(ProtocolConstants.CMD_SIGN_IN, name);
            client.readFromServer();
            

            Boolean exitFlag = false;

            while (!exitFlag) {

                System.out.println("\nSelect from the following options:\n"
                                  + "1. Play a new game\n"
                                  + "2. View statistics\n"
                                  + "3. Exit\n");
                String option = System.console().readLine();

                switch (option) {
                    case "1":
                        System.out.println("\nStarting a new game...");
                        client.playPuzzle();
                        break;
                
                    case "2":
                        System.out.println("\nViewing statistics...");
                        break;

                    case "3":
                        System.out.println("\nGoodbye!");
                        client.sendToServer(ProtocolConstants.CMD_EXIT, "dummyValue");
                        exitFlag = true;
                        break;
                    
                }
            }

            client.closeClient();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void closeClient() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    private void sendToServer(byte cmdCode, String message)
    {
        try
        {
            String fullMessage = String.format("%02X %s%s", cmdCode, message, ProtocolConstants.MSG_TERMINATOR);
            toServer.print(fullMessage);
            toServer.flush();
        }
        catch(Exception e)
        {
            System.out.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void readFromServer() {

        String response = null;

        try {
            response = fromServer.readLine();
            byte cmdCode = parseCommand(response);
            String contents = parseContents(response);

            handleResponse(cmdCode, contents);

        } catch (IOException e) {
            System.out.println("Error reading from server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
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

    private void handleResponse(byte cmdCode, String contents)
    {
        switch (cmdCode)
        {
            case ProtocolConstants.CMD_SND_Mischellaneous:
            displayServerMessage(contents);
            break;

            case ProtocolConstants.CMD_SND_PUZZLE:
            System.out.println("\nGAME STATE PLAY\n"
                             + "Puzzle:\n"
                             + contents);
        }

    }

    public void instructionsMessage()
    {
        System.out.println("\nPlease guess a letter or a word (enter '~' to return to menu):\n"
        + "you can also verify if a word exists by prefixing a word with '?' eg. ?apple\n"
        + "add words to the repo by prefixing a word with '+'\n"
        + "remove words from the repo by prefixing a word with '-' \n");

    }

    private void displayServerMessage(String contents)
    {
        System.out.println(contents);
    }

    private void handleUserInput(String input)
    {
       String symbol = Character.toString(input.charAt(0));
       String word;

       switch(symbol)
       {
        case "?":
        word = input.substring(1);
        sendToServer(ProtocolConstants.CMD_CHECK_IF_WORD_EXISTS, word);
        readFromServer();
        break;

        case "+":
        word = input.substring(1);
        sendToServer(ProtocolConstants.CMD_ADD_WORD, word);
        readFromServer();
        break;

        case "-":
        word = input.substring(1);
        sendToServer(ProtocolConstants.CMD_REMOVE_WORD, word);
        readFromServer();
        break;

        default:
        System.out.println("Guessing /"+input+"/ ");
        sendToServer(ProtocolConstants.CMD_SUBMIT_GUESS, input);
        readFromServer();
       }
    }

    private String getNumberOfWords()
    {
        //logic for number of words
    }

    private String getFactor()
    {
        //logic for number of words
    }

    private void playPuzzle() {

        try{
        System.out.println("\nHow many words would you like in the puzzle? (Enter a number between 1 and 5)");
        String numWords = getNumberOfWords();

        System.out.println("\nEnter a failed attempt factor (Enter a number between 1 and 5)");
        String failedAttemptFactor = getFactor();

        this.sendToServer(ProtocolConstants.CMD_LEVEL_SET,numWords + ":" + failedAttemptFactor);
        readFromServer(); //server sends empty puzzle 
        
        instructionsMessage();
        String input = System.console().readLine();
        handleUserInput(input);

        while(input != "~")
        {
        instructionsMessage();
        input = System.console().readLine();
        handleUserInput(input);
        }
    }
    catch(Exception e)
    {
        System.out.println("Error in playpuzzle");
    }
        //handle going back to menu logic
        

    }

}
