import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        Map<String, Integer> users = new ConcurrentHashMap<>();

        try {
            System.out.println("The game server is running...");
            serverSocket = new ServerSocket(8080);
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
            while (true) {
				fixedThreadPool.execute(new ClientHandler(serverSocket.accept(), users));
			}
        } catch (IOException e) {
            System.out.println(e);
            try {
                serverSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } 
    }

    public static class ClientHandler implements Runnable{

        private Socket clientSocket;
        private BufferedReader fromClient;
        private PrintStream toClient;
        private Map<String, Integer> usersMap;
        private String user;
        private PuzzleObject puzzle;

        ClientHandler(Socket socket, Map<String, Integer> users) {

			this.clientSocket = socket;
            this.usersMap = users;

            try {
                fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                toClient = new PrintStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                System.out.println(e);
                System.exit(1);
            }
		}

        /**
         * Handles client requests and sends responses back to the client. 
         * The run() method is called by the ExecutorService after a client has connected to the server.
         * It handles the communication between the client and the server.
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {

            System.out.println("Connected, handling new client: " + clientSocket);

            String message;
            String[] parts;

            try {
                message = fromClient.readLine();
            
                while(message != null) {

                    parts = message.split(" ");

                    switch (parts[0]) {
                        
                        case Constants.CMD_EXIT:
                            handleClientExit();
                            break;

                        case Constants.CMD_SIGN_IN:
                            handleClientSignIn(parts[1]);
                            break;

                        case Constants.CMD_LEVEL_SET:
                            handleLevelSet(parts[1]);
                            break;
                    
                        case Constants.CMD_SUBMIT_GUESS:
                            handleSubmitGuess(parts[1]);
                            break;
                        
                        case Constants.CMD_ABORT_GAME:
                            handleEndGame(false);
                            break;

                        case Constants.CMD_CHECK_SCORE:
                            handleViewStatistics();
                            break;

                    }
                    message = fromClient.readLine();
                }
            } catch (Exception e) {
                System.out.println(e);
                handleClientExitServerError();
            }
            handleClientExit();
        }

        /**
         * Sends a command and message to the client.
         * The message is formatted by concatenating the command code
         * and the message with a space separator, then sent through
         * the established PrintStream to the client.
         * 
         * @param cmdCode The command code to be sent to the client.
         * @param message The message associated with the command.
         */
        private void sendMessage(String cmdCode, String message){
            try {
                this.toClient.println(cmdCode + " " + message);
            }
            catch(Exception e){
                System.out.println("Error sending message: " + e.getMessage());
                e.printStackTrace();
            }
        }

        /**
         * Closes the client socket connection. 
         * This method attempts to close the socket and catches any IOException that occurs, 
         * printing the error message to the console.
         */
        private void handleClientExit(){
            try { 
                clientSocket.close();
                System.out.println("Client connection closed: " + clientSocket);
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }

        /**
         * Handles client exit when a server error occurs.
         * Sends an error message to the client indicating a server error
         * and advises the client to try again later. Closes the client's
         * socket connection and logs the closure to the console. If an
         * IOException occurs while closing the socket, logs the error message.
         */
        private void handleClientExitServerError(){
            try { 
                sendMessage(Constants.CMD_SND_ERROR, "Server error, please try again later.");
                clientSocket.close();
                System.out.println("Client connection closed: " + clientSocket);
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }

        /**
         * Handles client sign-in by checking if the user is already registered.
         * If the user exists, sends a welcome back message to the client.
         * If the user is new, adds the user to the database and sends a welcome message.
         * Updates the current user for the client session.
         * 
         * @param user The username provided by the client for sign-in.
         */
        private void handleClientSignIn(String user){

            try{            
                if (usersMap.containsKey(user)) {
                    sendMessage(Constants.CMD_SND_MISCELLANEOUS, "Welcome back " + user + " to the Word Puzzle Game Server!");
                    System.out.println("user '" + user + "' already in database " + clientSocket);
                } else {
                    usersMap.put(user, 0);
                    sendMessage(Constants.CMD_SND_MISCELLANEOUS, "Welcome " + user + " to the Word Puzzle Game Server!");
                    System.out.println("added '" + user + "' to database " + clientSocket);
                }
                this.user = user;
            }
            catch(Exception e){
                System.out.println("Error with client sign in: "+ e.getMessage());
                e.printStackTrace();
            }
        }

        /**
         * Handles client level set request by creating a new puzzle with the given
         * number of words and difficulty factor. It then sends the puzzle to the client
         * by calling sendMessage with the command code CMD_SND_PUZZLE and the puzzle
         * string as the message.
         * 
         * @param args The message containing the number of words and difficulty factor
         *             separated by a colon (:) delimiter.
         */
        private void handleLevelSet(String args){

            int numOfWords = Integer.parseInt(args.split(":")[0]);
            int difficultyFactor = Integer.parseInt(args.split(":")[1]);

            System.out.println("Setting up puzzle with " + numOfWords + " number of words and a difficulty factor of " + difficultyFactor); 
            
            try {
                this.puzzle = new PuzzleObject(numOfWords, difficultyFactor);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                this.handleClientExitServerError();
            }

            sendMessage(Constants.CMD_SND_PUZZLE, puzzle.getPuzzleSlaveString());
        }

        /**
         * Handles client guess submission by passing the guess to the puzzle object's guess
         * methods. If the guess is a single character, it calls the guessChar method.
         * If the guess is a word, it calls the guessWord method.
         * Depending on the result of the guess, it sends a message back to the client
         * with the command code CMD_SND_PUZZLE and the puzzle string as the message.
         * If the puzzle is solved, it sends the CMD_SND_GAMEWIN message. If the puzzle is
         * not solved and the guess counter is zero, it sends the CMD_SND_GAMELOSS message.
         * Finally, it ends the game by calling the handleEndGame method.
         * 
         * @param guess The guess made by the client, either a single character or a word.
         */
        private void handleSubmitGuess(String guess){

            String trimmedGuess = guess.trim();
            Boolean solvedFlag;

            if (trimmedGuess.length() == 1) {

                solvedFlag = puzzle.guessChar(trimmedGuess.charAt(0));

                if (!solvedFlag) {
                    if (puzzle.getGuessCounter() == 0) {
                        this.handleEndGame(false);
                        sendMessage(Constants.CMD_SND_GAMELOSS, puzzle.getPuzzleSlaveString());
                    } else {
                        sendMessage(Constants.CMD_SND_PUZZLE, puzzle.getPuzzleSlaveString());
                    }
                } else {
                    sendMessage(Constants.CMD_SND_GAMEWIN, puzzle.getPuzzleSlaveString());
                    this.handleEndGame(true);
                }
            } else {

                solvedFlag = puzzle.guessWord(trimmedGuess);

                if (!solvedFlag) {
                    if (puzzle.getGuessCounter() == 0) {
                        this.handleEndGame(false);
                        sendMessage(Constants.CMD_SND_GAMELOSS, puzzle.getPuzzleSlaveString());
                        
                    } else {
                        sendMessage(Constants.CMD_SND_PUZZLE, puzzle.getPuzzleSlaveString());
                    }
                } else {
                    sendMessage(Constants.CMD_SND_GAMEWIN, puzzle.getPuzzleSlaveString());
                    this.handleEndGame(true);
                }
            }
        }

        /**
         * Handles the end of the game by updating the user's score in the map.
         * If the game was won, it adds 1 point to the user's score. If the game
         * was lost, it subtracts 1 point from the user's score.
         * 
         * @param win A boolean indicating if the user won or lost the game.
         */
        private void handleEndGame(Boolean win){

            if (win) {
                usersMap.put(user, usersMap.get(user) + 1);
                System.out.println("User " + user + " won the game! 1 point added to their score.");
            } else {
                usersMap.put(user, usersMap.get(user) - 1);
                System.out.println("User " + user + " lost the game. 1 point removed from their score.");
            }

        }

        /**
         * Handles the user's score query by sending the current score to the user.
         */
        private void handleViewStatistics(){
            sendMessage(Constants.CMD_SND_SCORE, usersMap.get(user).toString());
        }
    }
}
