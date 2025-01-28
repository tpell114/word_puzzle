import java.io.*;
import java.net.Socket;

public class Client {

    private Socket clientSocket;

    public Client() {
        try {
            clientSocket = new Socket("localhost", 8080);
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void main(String[] args) throws Exception {
        
        try {
            Client client = new Client();

            System.out.println("\nWelcome to Word Puzzle!");
            System.out.println("=======================");
            System.out.println("\nPlease enter your name:");
            String name = System.console().readLine();
            client.sendToServer(ProtocolConstants.CMD_SIGN_IN + " " + name);
            String response = client.readFromServer();
            System.out.println("\n" + response);

            Boolean exitFlag = false;

            while (!exitFlag) {

                System.out.println("\nSelect from the following options:");
                System.out.println("1. Play a new game");
                System.out.println("2. View statistics");
                System.out.println("3. Exit");
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
                        client.sendToServer(ProtocolConstants.CMD_EXIT + " ");
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


    private void sendToServer(String request) {
		//System.out.println("Sending the request: " + request + " to the server!");
		try {
			PrintStream toServer = new PrintStream(clientSocket.getOutputStream());
			toServer.println(request);	
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

    private String readFromServer() {

        String response = null;

        try {
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            response = fromServer.readLine();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return response;
    }

    private void playPuzzle() {

        System.out.println("\nHow many words would you like in the puzzle? (Enter a number between 1 and 5)");
        String numWords = System.console().readLine();
        System.out.println("\nEnter a failed attempt factor (Enter a number between 1 and 5)");
        String failedAttemptFactor = System.console().readLine();
        this.sendToServer(ProtocolConstants.CMD_REQ_NEW_GAME + " " + numWords + ":" + failedAttemptFactor);
        String response = this.readFromServer();
        System.out.println("\n" + response);

        //get initial puzzle
        String puzzle = this.readFromServer();
        System.out.println("\n" + puzzle);
        String[] puzzleLines = puzzle.split("\\+");

        System.out.println("\nGAME STATE PLAY");
        System.out.println("Puzzle:");

        for (int i = 0; i < puzzleLines.length; i++) {
            System.out.println(puzzleLines[i]);
        }

        
        System.out.println("\nPlease guess a letter or a word (enter ~ to return to menu):");
        String guess = System.console().readLine();

        while (!guess.equals("~")) {

            System.out.println("\nPlease guess a letter or a word (enter ~ to return to menu):");
            guess = System.console().readLine();
        }


    }

}
