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
        private String formattedPuzzle; 
        private String hiddenPuzzle;
        public WorkerThread(Socket clientSocket)
        {
            this.clientSocket = clientSocket;
        }

        private void handleClientReq(byte cmdCode, String message, PrintStream out, Socket clientSocket)
        {
            try {
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
                    String[] difficulty = message.split(":",2);
    
                    if (difficulty.length < 2) {
                        throw new IllegalArgumentException("Input does not contain the expected ':' delimiter.");
                    }
                    String numOfWords = difficulty[0].trim();
                    int factor =  Integer.parseInt(difficulty[1].trim());
    
                    System.out.println("Contacting word repository to setup level with " + numOfWords + "number or words" + "and a difficulty factor of" + factor); 
                    handleSetupLevel(out, numOfWords, factor);   
                 }
                catch(Exception e)
                {
                    System.out.println("Invalid level set parameters: " + message);
                    sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, "Error: Invalid level parameters. Use format 'n:f'");
                }
                break;
    
                case ProtocolConstants.CMD_SUBMIT_GUESS:

                    try {
                        if (formattedPuzzle == null || hiddenPuzzle == null) {
                            sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, "ERROR: No active puzzle. Start a new game first.");
                            return;
                        }
                    String updatedPuzzle = updatePuzzle(formattedPuzzle, hiddenPuzzle, message);
                    hiddenPuzzle = updatedPuzzle; // Update the hidden puzzle state
                    sendMessage(out, ProtocolConstants.CMD_SND_PUZZLE, hiddenPuzzle);
                    }
                 catch (Exception e) {
                    System.out.println("Error processing guess: " + message);
                    sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, "ERROR: Invalid guess.");
                }
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
    try {
        if (message == null || message.isEmpty()) {
            sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, "ERROR: No word provided.");
            break;
        }

        String exists = contactWordRepository(cmdCode, message);

        if (exists == null || exists.equals("ERROR")) {
            sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, "ERROR: Word repository not responding.");
        } else {
            sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, "Does the word " + message + " exist?: " + exists);
        }
    } catch (Exception e) {
        System.out.println("Error verifying word with repo: " + message);
        e.printStackTrace();
        sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, "ERROR: Word verification failed.");
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
        catch(Exception e)
        {
            System.out.println("CRITICAL ERROR in handleClientReq: " + e.getMessage());
            e.printStackTrace();
        }
        }
    

        private void handleSetupLevel(PrintStream out, String numOfWords, int factor)
        {
            String stem = contactWordRepository(ProtocolConstants.CMD_GET_STEM_WORD, numOfWords);
    
            System.out.println("STEMWORD IS ::: " + stem);
    
            ArrayList<String> solvedPuzzle = PuzzleObject.generatePuzzle(stem, numOfWords);
            
            formattedPuzzle =formatPuzzle(solvedPuzzle, stem);
            hiddenPuzzle = hidePuzzle(formattedPuzzle);
    
            String formattedPuzzle = formatPuzzle(solvedPuzzle, stem);
           
            System.out.println(solvedPuzzle+ "\n\n");
    
            System.out.println(formattedPuzzle);

            sendMessage(out, ProtocolConstants.CMD_SND_MISCELLANEOUS, hiddenPuzzle); //client doesnt recieve puzzle cmdsndpuzzle
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
                System.out.println("byte is:" + cmdCode);
                String msgContents = parseContents(inputMsg);
                System.out.println("contents is:" + msgContents);
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
            return parts[1].trim();  
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

    public static String getRandomWord()
    {
        return contactWordRepository(ProtocolConstants.CMD_GET_RANDOM_WORD, "dummy");
    }

        public String formatPuzzle(ArrayList<String> words, String stem) {
            int maxWordLength = words.stream().mapToInt(String::length).max().orElse(0);
            int totalWidth = maxWordLength + stem.length() + 5; // Extra padding
            int stemColumn = totalWidth / 2; // Fixed column for vertical stem
        
            ArrayList<StringBuilder> grid = new ArrayList<>();
        
            // Step 1: Initialize empty grid with spaces and place vertical stem letters
            for (int i = 0; i < stem.length(); i++) {
                grid.add(new StringBuilder(".".repeat(totalWidth))); // Fill row with placeholders
            }
        
            // Step 2: Place stem letters in the fixed column
            for (int i = 0; i < stem.length(); i++) {
                grid.get(i).setCharAt(stemColumn, stem.charAt(i)); // Place stem letter
            }
        
            // Step 3: Align horizontal words based on where they match the stem
            for (String word : words) {
                int row = findAlignmentPosition(stem, word);
                if (row != -1 && row < grid.size()) {
                    int matchingLetterIndex = -1;
        
                    // Find first letter in word that matches the stem
                    for (int i = 0; i < word.length(); i++) {
                        if (stem.indexOf(word.charAt(i)) == row) { // Ensure alignment with the correct row
                            matchingLetterIndex = i;
                            break;
                        }
                    }
        
                    if (matchingLetterIndex != -1) {
                        // The word must start so that the matching letter aligns with the stem column
                        int wordStartColumn = stemColumn - matchingLetterIndex;
        
                        // Ensure the word fits within bounds
                        wordStartColumn = Math.max(0, wordStartColumn);
                        int wordEndColumn = Math.min(totalWidth, wordStartColumn + word.length());
        
                        // Place the word at the correct position in the row
                        for (int j = 0; j < word.length() && wordStartColumn + j < wordEndColumn; j++) {
                            grid.get(row).setCharAt(wordStartColumn + j, word.charAt(j));
                        }
                    }
                }
            }
        
            // Step 4: Append "+" to the end of each row for formatting
            for (StringBuilder row : grid) {
                row.append(" +");
            }
        
            // Step 5: Convert grid to a single formatted string
            StringBuilder formattedCrossword = new StringBuilder();
            for (StringBuilder row : grid) {
                formattedCrossword.append(row.toString().stripTrailing());
            }
        
            return formattedCrossword.toString();
        }
        

    private static int findAlignmentPosition(String stem, String word) {
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            int indexInStem = stem.indexOf(letter); // Find first match in stem
    
            if (indexInStem != -1) {
                return indexInStem; // Return row index where word should start
            }
        }
        return -1; // If no match found, return -1
    }

    public String hidePuzzle(String formattedPuzzle) {
        StringBuilder hiddenPuzzle = new StringBuilder();
    
        for (char c : formattedPuzzle.toCharArray()) {
            if (Character.isLetter(c)) {
                hiddenPuzzle.append('_'); // Replace letters with underscores
            } else {
                hiddenPuzzle.append(c); // Keep spaces, dots, and formatting symbols
            }
        }
    
        return hiddenPuzzle.toString();
    }

    public String updatePuzzle(String formattedPuzzle, String hiddenPuzzle, String guess) {
        StringBuilder updatedPuzzle = new StringBuilder(hiddenPuzzle);
    
        for (int i = 0; i < formattedPuzzle.length(); i++) {
            char originalChar = formattedPuzzle.charAt(i);
            char hiddenChar = hiddenPuzzle.charAt(i);
    
            // Reveal only if it's a letter and matches the guess
            if (Character.isLetter(originalChar) && (guess.equalsIgnoreCase(String.valueOf(originalChar)) || guess.equalsIgnoreCase(formattedPuzzle.substring(i, Math.min(i + guess.length(), formattedPuzzle.length()))))) {
                updatedPuzzle.setCharAt(i, originalChar);
            }
        }
    
        return updatedPuzzle.toString();
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


    private static String contactWordRepository(byte cmdCode, String message)
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
            socket.setSoTimeout(3000); 
            socket.receive(response);

            String result = new String(response.getData(), 0, response.getLength()).trim();
            if(result.isEmpty()) return "ERROR: Empty response";

            return result;

        }
        catch (IOException e) 
        {
            System.err.println("Error communicating with WordRepoMicroservice: " + e.getMessage());
            return "ERROR"; 
        }

    }
    }




