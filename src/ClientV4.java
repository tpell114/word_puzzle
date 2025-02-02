import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ClientV4 {

    private Integer port = 8080;
    private String host = "localhost";
    private Socket clientSocket;
    private PrintStream toServer;
    private BufferedReader fromServer;

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
        System.out.println("=====Sending: " + cmdCode + " " + message);
	}

    private void readFromServer() {

        String[] response = null;
        String rawResponse = null;

        try {
            rawResponse = fromServer.readLine();
            System.out.println("=====Received: " + rawResponse);
            response = rawResponse.split(" ", 2);
            this.handleResponse(response[0], response[1]);

        } catch (IOException e) {
            e.printStackTrace();
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
                //todo
                break;
        }
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

        System.out.println("\nHow many words would you like in the puzzle? (Enter a number between 1 and 5)");
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
                sendToServer(ProtocolConstantsV2.CMD_CHECK_IF_WORD_EXISTS, guess.substring(1));
                this.readFromServer();
            } else {
                this.sendToServer(ProtocolConstantsV2.CMD_SUBMIT_GUESS, guess);
                this.readFromServer();
                System.out.println("\nPlease guess a letter or a word (enter ~ to return to menu):"
                                    + "you can also verify if a word exists by prefixing a word with '?' eg. ?apple\n");
                guess = System.console().readLine();
            }
        }
    }

    private void modifyWordRepo() {

        System.out.println("\nAdd words to the repo by prefixing a word with '+'  eg. +apple\n"
                            + "remove words from the repo by prefixing a word with '-' eg. -tomato\n");

        String input = System.console().readLine();

        if (input.charAt(0) == '+') {
            this.sendToServer(ProtocolConstantsV2.CMD_ADD_WORD, input.substring(1));
            this.readFromServer();
        } else if (input.charAt(0) == '-') {
            this.sendToServer(ProtocolConstantsV2.CMD_REMOVE_WORD, input.substring(1));
            this.readFromServer();
        }
    }
}
