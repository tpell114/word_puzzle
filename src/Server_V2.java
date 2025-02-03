import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server_V2 {

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
        } 
    }

    public static class ClientHandler implements Runnable{

        private Socket clientSocket;
        private BufferedReader fromClient;
        private PrintStream toClient;
        private Map<String, Integer> usersMap;
        private String user;
        private PuzzleObjectV3 puzzle;

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

        @Override
        public void run() {

            System.out.println("Connected, handling new client: " + clientSocket);

            String message;
            String[] parts;

            try {
                message = fromClient.readLine();
            
                while(message != null) {
                    //System.out.println("=====Received: " + message); //FOR TESTING

                    parts = message.split(" ");

                    switch (parts[0]) {
                        
                        case ProtocolConstantsV2.CMD_EXIT:
                            handleClientExit();
                            break;

                        case ProtocolConstantsV2.CMD_SIGN_IN:
                            handleClientSignIn(parts[1]);
                            break;

                        case ProtocolConstantsV2.CMD_LEVEL_SET:
                            handleLevelSet(parts[1]);
                            break;
                    
                        case ProtocolConstantsV2.CMD_SUBMIT_GUESS:
                            handleSubmitGuess(parts[1]);
                            break;
                        
                        case ProtocolConstantsV2.CMD_ABORT_GAME:
                            handleEndGame(false);

                        case ProtocolConstantsV2.CMD_CHECK_SCORE:
                            handleViewStatistics();

                        default:
                            break;
                    }

                    message = fromClient.readLine();
                }

            } catch (Exception e) {
                System.out.println(e);
            } finally {
                handleClientExit();
            }

        }

        private void sendMessage(String cmdCode, String message){
            try
            {
                this.toClient.println(cmdCode + " " + message);
                //System.out.println("=====Sending: " + cmdCode + " " + message);
            }
            catch(Exception e)
            {
                System.out.println("Error sending message: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleClientExit(){
            try { 
                clientSocket.close();
                System.out.println("Client connection closed" + clientSocket);
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }

        private void handleClientSignIn(String user){

            try{            
                if (usersMap.containsKey(user)) {
                    sendMessage(ProtocolConstantsV2.CMD_SND_MISCELLANEOUS, "Welcome back " + user + " to the Word Puzzle Game Server!");
                    System.out.println("user '" + user + "' already in database " + clientSocket);
                } else {
                    usersMap.put(user, 0);
                    sendMessage(ProtocolConstantsV2.CMD_SND_MISCELLANEOUS, "Welcome " + user + " to the Word Puzzle Game Server!");
                    System.out.println("added '" + user + "' to database " + clientSocket);
                }
                this.user = user;
            }
            catch(Exception e){
                System.out.println("Error with client sign in: "+ e.getMessage());
                e.printStackTrace();
            }

        }

        private void handleLevelSet(String args){

            int numOfWords = Integer.parseInt(args.split(":")[0]);
            int difficultyFactor = Integer.parseInt(args.split(":")[1]);

            System.out.println("Setting up puzzle with " + numOfWords + " number of words and a difficulty factor of " + difficultyFactor); 
            puzzle = new PuzzleObjectV3(numOfWords, difficultyFactor);

            sendMessage(ProtocolConstantsV2.CMD_SND_PUZZLE, puzzle.getPuzzleSlaveString());
        }

        private void handleSubmitGuess(String guess){

            String trimmedGuess = guess.trim();
            Boolean solvedFlag;

            if (trimmedGuess.length() == 1) {

                solvedFlag = puzzle.guessChar(trimmedGuess.charAt(0));

                if (!solvedFlag) {
                    if (puzzle.getGuessCounter() == 0) {
                        this.handleEndGame(false);
                        sendMessage(ProtocolConstantsV2.CMD_SND_GAMELOSS, puzzle.getPuzzleSlaveString());
                    } else {
                        sendMessage(ProtocolConstantsV2.CMD_SND_PUZZLE, puzzle.getPuzzleSlaveString());
                    }
                } else {
                    sendMessage(ProtocolConstantsV2.CMD_SND_GAMEWIN, puzzle.getPuzzleSlaveString());
                    this.handleEndGame(true);
                }
            } else {

                solvedFlag = puzzle.guessWord(trimmedGuess);

                if (!solvedFlag) {
                    if (puzzle.getGuessCounter() == 0) {
                        this.handleEndGame(false);
                        sendMessage(ProtocolConstantsV2.CMD_SND_GAMELOSS, puzzle.getPuzzleSlaveString());
                        
                    } else {
                        sendMessage(ProtocolConstantsV2.CMD_SND_PUZZLE, puzzle.getPuzzleSlaveString());
                    }
                } else {
                    sendMessage(ProtocolConstantsV2.CMD_SND_GAMEWIN, puzzle.getPuzzleSlaveString());
                    this.handleEndGame(true);
                }
            }

        }

        private void handleEndGame(Boolean win){

            if (win) {
                usersMap.put(user, usersMap.get(user) + 1);
                System.out.println("User " + user + " won the game! 1 point added to their score.");
            } else {
                usersMap.put(user, usersMap.get(user) - 1);
                System.out.println("User " + user + " lost the game. 1 point removed from their score.");
            }

        }

        private void handleViewStatistics(){
            sendMessage(ProtocolConstantsV2.CMD_SND_SCORE, usersMap.get(user).toString());
        }

    }

    

}
