import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    private Integer port = 8080;
    private String host = "localhost";
    private Socket clientSocket;
    private PrintStream toServer;
    private BufferedReader fromServer;
    private Boolean gameOverFlag;

    public Client() {
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

        Client client = new Client();

        try {
            client.userSignIn();

            Boolean exitFlag = false;
            String option;

            while (!exitFlag) {

                System.out.println(Constants.MAIN_MENU_MESSAGE);
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
                        client.sendToServer(Constants.CMD_EXIT, "\0");
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

    /**
     * Closes the client socket connection. 
     * This method attempts to close the socket and catches any IOException that occurs, 
     * printing the error message to the console.
     */
    public void closeClient() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Closes the client socket connection and exits the program.
     * This method attempts to close the socket and if an IOException occurs,
     * it prints the error message to the console and exits with status 1.
     */
    public void closeClientError() {
        try {
            clientSocket.close();
            System.exit(1);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    /**
     * Sends a command and message to the server.
     * The message is formatted by concatenating the command code
     * and the message with a space separator, then sent through
     * the established PrintStream to the server.
     *
     * @param cmdCode The command code to be sent to the server.
     * @param message The message associated with the command.
     */
    private void sendToServer(String cmdCode, String message) {
		toServer.println(cmdCode + " " + message);	
	}
    
    /**
     * Reads a response from the server and parses it into a command and message.
     * The command and message are split by a space separator and passed to
     * the handleResponse method to handle the command.
     * 
     * @throws IOException If an error occurs while reading from the server.
     */
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

    /**
     * Handles the server response based on the command code received.
     * Depending on the command code, it either prints the content, displays
     * the puzzle, handles game win or loss scenarios, or shows the current score.
     *
     * @param cmdCode The command code indicating the type of response.
     * @param contents The content or message associated with the command.
     */
    private void handleResponse(String cmdCode, String contents)
    {
        switch (cmdCode)
        {
            case Constants.CMD_SND_MISCELLANEOUS:
                System.out.println(contents);
                break;

            case Constants.CMD_SND_PUZZLE:
                this.printPuzzle(contents);
                break;

            case Constants.CMD_SND_GAMEWIN:
                this.handleGameWin(contents);
                break;

            case Constants.CMD_SND_GAMELOSS:
                this.handleGameLoss(contents);
                break;

            case Constants.CMD_SND_SCORE:
                System.out.println("Your current score is: " + contents);
                break;

            case Constants.CMD_SND_ERROR:
                System.out.println(contents);
                this.closeClientError();
                break;
        }
    }

    /**
     * Prints the puzzle to the console, split into its individual lines.
     * This method takes a string containing the puzzle, splits it into
     * individual lines using the '+' character as a separator, and prints
     * each line to the console with a newline separator.
     *
     * @param contents The puzzle contents to be printed.
     */
    private void printPuzzle(String contents) {
        
        String[] lines = contents.split("\\+");

        System.out.println();
        for (String line : lines) {
            System.out.println(line);
        }
    }

    /**
     * Handles the server's game win message. Prints the final puzzle to the
     * console and announces that the game is won.
     *
     * @param contents The puzzle contents to be printed.
     */
    private void handleGameWin(String contents) {
        this.printPuzzle(contents);
        System.out.println("\nYou have won the game! 1 point added to your score.");
        gameOverFlag = true;
    }

    /**
     * Handles the server's game loss message. Prints the final puzzle to the
     * console, announces that the game is lost.
     *
     * @param contents The puzzle contents to be printed.
     */
    private void handleGameLoss(String contents) {
        this.printPuzzle(contents);
        System.out.println("\nYou have lost the game. 1 point deducted from your score.");
        gameOverFlag = true;
    }

    /**
     * Prompts the user to enter their name for signing into the server.
     * The entered name is sent to the server using the sign-in command.
     * After sending, it waits and reads the server's response.
     */
    private void userSignIn() {
        System.out.println(Constants.USER_SIGN_IN_MESSAGE);
        String name = System.console().readLine();
        this.sendToServer(Constants.CMD_SIGN_IN, name);
        this.readFromServer();
    }

    /**
     * Starts a new game of Word Puzzle.
     * Prompts the user to enter the number of words and the failed attempt factor.
     * Then, it sends the input to the server and waits for the server's response.
     * After receiving the response, it prints the puzzle to the console and
     * prompts the user to guess a letter or a word.
     * If the guess is a word, it checks if the word exists in the word repository
     * and prints the result to the console.
     * If the guess is a letter, it sends the guess to the server and waits for
     * the server's response.
     * If the server's response is that the game is over, it prints the final
     * puzzle to the console and announces the result of the game.
     * If the server's response is that the game is not over, it prints the updated
     * puzzle to the console and prompts the user to guess again.
     * If the user enters ~ to abort the game, it sends the abort command to the
     * server and ends the game.
     */
    private void playPuzzle() {

        gameOverFlag = false;
        System.out.println("\nHow many words would you like in the puzzle? (Enter a number between 2 and 5)");
        String numWords = System.console().readLine();
        System.out.println("\nEnter a failed attempt factor (Enter a number between 1 and 5)");
        String failedAttemptFactor = System.console().readLine();
        this.sendToServer(Constants.CMD_LEVEL_SET, numWords + ":" + failedAttemptFactor);
        this.readFromServer();

        System.out.println(Constants.GUESS_MESSAGE);
        String guess = System.console().readLine().toLowerCase().trim();

        while (guess.equals("")) {

            System.out.println(Constants.GUESS_MESSAGE);
            guess = System.console().readLine().toLowerCase().trim();
        }

        while (!guess.equals("~")) {

            if (guess.charAt(0) == '?') {
                String wordExists = this.contactWordRepository(Constants.CMD_CHECK_IF_WORD_EXISTS, guess.substring(1));
                
                if (wordExists.equals("0")) {
                    System.out.println("\nWord '" + guess.substring(1) + "' does not exist in the word repository.");
                } else if (wordExists.equals("1")) {
                    System.out.println("\nWord '" + guess.substring(1) + "' exists in the word repository.");
                }

                System.out.println(Constants.GUESS_MESSAGE);
                guess = System.console().readLine();
            } else {
                this.sendToServer(Constants.CMD_SUBMIT_GUESS, guess);
                this.readFromServer();
                if (gameOverFlag) break;
                System.out.println(Constants.GUESS_MESSAGE);
                guess = System.console().readLine();
            }
        }

        if (guess.equals("~")) {
            this.sendToServer(Constants.CMD_ABORT_GAME, "\0");
        }
    }

    /**
     * Modifies the word repository by allowing the user to add or remove words
     * from the repository.
     * The user is prompted to enter a command to add or remove a word.
     * The command is either '+' to add a word or '-' to remove a word.
     * The user is also prompted to enter a word to check if it exists in the
     * repository.
     * The user is then prompted to enter a new command or '~' to exit the
     * word repository modification interface.
     */
    private void modifyWordRepo() {

        System.out.println(Constants.WORD_REPO_MESSAGE);

        String input = System.console().readLine();

        while (!input.equals("~")) {

            if (input.charAt(0) == '+') {

                String success = this.contactWordRepository(Constants.CMD_ADD_WORD, input.substring(1));
                
                if (success.equals("0")) {
                    System.out.println("\nFailed to add word '" + input.substring(1) + "' to the word repository, it may already exist.");
                } else if (success.equals("1")) {
                    System.out.println("\nSuccessfully added word '" + input.substring(1) + "' to the word repository.");
                }

            } else if (input.charAt(0) == '-') {

                String success = this.contactWordRepository(Constants.CMD_REMOVE_WORD, input.substring(1));
                
                if (success.equals("0")) {
                    System.out.println("\nFailed to remove word '" + input.substring(1) + "' from the word repository, it may not exist.");
                } else if (success.equals("1")) {
                    System.out.println("\nSuccessfully removed word '" + input.substring(1) + "' from the word repository.");
                }
                
            } else if (input.charAt(0) == '?') {

                String wordExists = this.contactWordRepository(Constants.CMD_CHECK_IF_WORD_EXISTS, input.substring(1));
                
                if (wordExists.equals("0")) {
                    System.out.println("\nWord '" + input.substring(1) + "' does not exist in the word repository.");
                } else if (wordExists.equals("1")) {
                    System.out.println("\nWord '" + input.substring(1) + "' exists in the word repository.");
                }
            }

            System.out.println(Constants.WORD_REPO_MESSAGE);
            input = System.console().readLine();
        }
    }

    /**
     * Requests the server to send the user's current score.
     * The command sent to the server is CMD_CHECK_SCORE.
     * The user is not prompted for any input.
     * The server's response is printed to the console.
     */
    private void viewStatistics() {
        this.sendToServer(Constants.CMD_CHECK_SCORE, "\0");
        this.readFromServer();
    }

    /**
     * Communicates with the Word Repository microservice via UDP to send a command and message.
     * Constructs a message by appending a command code and message with a terminator,
     * then sends it to a predefined address and port. Waits for a response from the microservice.
     * 
     * @param cmdCode The command code to be sent to the Word Repository microservice.
     * @param message The message associated with the command.
     * @return The response from the Word Repository microservice as a trimmed string.
     *         Returns "ERROR: Empty response" if the response received is empty.
     *         Returns "ERROR" if an IOException occurs during communication.
     */
    private String contactWordRepository(String cmdCode, String message){
            
        try(DatagramSocket socket = new DatagramSocket()){
            InetAddress address = InetAddress.getByName("localhost");
        
            String fullMessage = cmdCode + " " + message + Constants.MSG_TERMINATOR;
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
