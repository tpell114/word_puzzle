import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class ClientV4 {

    private Integer port = 8080;
    private String host = "localhost";
    private Socket clientSocket;
    private PrintStream toServer;
    private BufferedReader fromServer;
    private Boolean gameOverFlag;

    public ClientV4() {
        try {
            clientSocket = new Socket(host, port);
            toServer = new PrintStream(clientSocket.getOutputStream());
            fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {

        ClientV4 client = new ClientV4();

        try {
            client.userSignIn();

            Boolean exitFlag = false;
            String option;

            while (!exitFlag) {

                System.out.println("\nSelect from the following options:\n"
                                    +"1. Play a new game\n"
                                    +"2. View statistics\n"
                                    +"3. Modify word repository\n"
                                    +"4. Exit\n");
                option = System.console().readLine();

                switch (option) {
                    case "1":
                        System.out.println("\nStarting a new game...");
                        client.playPuzzle();
                        break;
                
                    case "2":
                        System.out.println("\nViewing statistics...");
                        client.viewStatistics();
                        break;

                    case "3":
                        System.out.println("\nModifying word repository...");
                        client.modifyWordRepo();
                        break;

                    case "4":
                        System.out.println("\nGoodbye!");
                        client.sendToServer(ProtocolConstantsV2.CMD_EXIT, "\0");
                        exitFlag = true;
                        break;

                }
            }
            
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            client.closeClient();
        }

    }

    public void closeClient() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void sendToServer(String cmdCode, String message) {
		toServer.println(cmdCode + " " + message);	
	}

    private void readFromServer() {

        String[] response = null;
        String rawResponse = null;

        try {
            rawResponse = fromServer.readLine();
            response = rawResponse.split(" ", 2);
            this.handleResponse(response[0], response[1]);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void handleResponse(String cmdCode, String contents)
    {
        switch (cmdCode)
        {
            case ProtocolConstantsV2.CMD_SND_MISCELLANEOUS:
                System.out.println(contents);
                break;

            case ProtocolConstantsV2.CMD_SND_PUZZLE:
                this.printPuzzle(contents);
                break;

            case ProtocolConstantsV2.CMD_SND_GAMEWIN:
                this.handleGameWin(contents);
                break;

            case ProtocolConstantsV2.CMD_SND_GAMELOSS:
                this.handleGameLoss(contents);
                break;

            case ProtocolConstantsV2.CMD_SND_SCORE:
                System.out.println("Your current score is: " + contents);
                break;
        }
    }

    private void printPuzzle(String contents) {
        String[] lines = contents.split("\\+");

        System.out.println();
        for (String line : lines) {
            System.out.println(line);
        }
    }

    private void handleGameWin(String contents) {
        this.printPuzzle(contents);
        System.out.println("\nYou have won the game! 1 point added to your score.");
        gameOverFlag = true;
    }

    private void handleGameLoss(String contents) {
        this.printPuzzle(contents);
        System.out.println("\nYou have lost the game. 1 point deducted from your score.");
        gameOverFlag = true;
    }

    private void userSignIn() {

        System.out.println("\nWelcome to Word Puzzle!\n"
                            +"=======================\n"
                            +"Please enter your name:\n");
        String name = System.console().readLine();
        this.sendToServer(ProtocolConstantsV2.CMD_SIGN_IN, name);
        this.readFromServer();
    }

    private void playPuzzle() {

        gameOverFlag = false;
        System.out.println("\nHow many words would you like in the puzzle? (Enter a number between 2 and 5)");
        String numWords = System.console().readLine();
        System.out.println("\nEnter a failed attempt factor (Enter a number between 1 and 5)");
        String failedAttemptFactor = System.console().readLine();
        this.sendToServer(ProtocolConstantsV2.CMD_LEVEL_SET, numWords + ":" + failedAttemptFactor);
        this.readFromServer();

        System.out.println("\nPlease guess a letter or a word (enter ~ to return to menu):"
                            + "you can also verify if a word exists by prefixing a word with '?' eg. ?apple\n");
        String guess = System.console().readLine();

        while (!guess.equals("~")) {

            if (guess.charAt(0) == '?') {
                String wordExists = this.contactWordRepository(ProtocolConstantsV2.CMD_CHECK_IF_WORD_EXISTS, guess.substring(1));
                
                if (wordExists.equals("0")) {
                    System.out.println("\nWord '" + guess.substring(1) + "' does not exist in the word repository.");
                } else if (wordExists.equals("1")) {
                    System.out.println("\nWord '" + guess.substring(1) + "' exists in the word repository.");
                }

                System.out.println("\nPlease guess a letter or a word (enter ~ to return to menu):"
                                    + "you can also verify if a word exists by prefixing a word with '?' eg. ?apple\n");
                guess = System.console().readLine();
            } else {
                this.sendToServer(ProtocolConstantsV2.CMD_SUBMIT_GUESS, guess);
                this.readFromServer();
                if (gameOverFlag) break;
                System.out.println("\nPlease guess a letter or a word (enter ~ to return to menu):"
                                    + "you can also verify if a word exists by prefixing a word with '?' eg. ?apple\n");
                guess = System.console().readLine();
            }
        }

        if (guess.equals("~")) {
            this.sendToServer(ProtocolConstantsV2.CMD_ABORT_GAME, "\0");
        }
    }

    private void modifyWordRepo() {

        System.out.println("\nAdd words to the repo by prefixing a word with '+'  eg. +apple\n"
                            + "remove words from the repo by prefixing a word with '-' eg. -apple\n"
                            + "check if a word exists by prefixing a word with '?' eg. ?apple\n"
                            + "enter '~' to return to menu");

        String input = System.console().readLine();

        while (!input.equals("~")) {

            if (input.charAt(0) == '+') {

                String success = this.contactWordRepository(ProtocolConstantsV2.CMD_ADD_WORD, input.substring(1));
                
                if (success.equals("0")) {
                    System.out.println("\nFailed to add word '" + input.substring(1) + "' to the word repository, it may already exist.");
                } else if (success.equals("1")) {
                    System.out.println("\nSuccessfully added word '" + input.substring(1) + "' to the word repository.");
                }

            } else if (input.charAt(0) == '-') {

                String success = this.contactWordRepository(ProtocolConstantsV2.CMD_REMOVE_WORD, input.substring(1));
                
                if (success.equals("0")) {
                    System.out.println("\nFailed to remove word '" + input.substring(1) + "' from the word repository, it may not exist.");
                } else if (success.equals("1")) {
                    System.out.println("\nSuccessfully removed word '" + input.substring(1) + "' from the word repository.");
                }
                
            } else if (input.charAt(0) == '?') {

                String wordExists = this.contactWordRepository(ProtocolConstantsV2.CMD_CHECK_IF_WORD_EXISTS, input.substring(1));
                
                if (wordExists.equals("0")) {
                    System.out.println("\nWord '" + input.substring(1) + "' does not exist in the word repository.");
                } else if (wordExists.equals("1")) {
                    System.out.println("\nWord '" + input.substring(1) + "' exists in the word repository.");
                }
            }

            System.out.println("\nAdd words to the repo by prefixing a word with '+'  eg. +apple\n"
                            + "remove words from the repo by prefixing a word with '-' eg. -apple\n"
                            + "check if a word exists by prefixing a word with '?' eg. ?apple\n"
                            + "enter '~' to return to menu");

            input = System.console().readLine();
        }
    }

    private void viewStatistics() {
        this.sendToServer(ProtocolConstantsV2.CMD_CHECK_SCORE, "\0");
        this.readFromServer();
    }

    private String contactWordRepository(String cmdCode, String message){
            
        try(DatagramSocket socket = new DatagramSocket()){
            InetAddress address = InetAddress.getByName("localhost");
        
            String fullMessage = cmdCode + " " + message + ProtocolConstantsV2.MSG_TERMINATOR;
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
        catch (IOException e) {
            System.err.println("Error communicating with WordRepoMicroservice: " + e.getMessage());
            return "ERROR"; 
        }

        
    }
    
}
