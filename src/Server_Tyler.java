import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;



public class Server_Tyler {
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
            System.exit(1);
        }
    }


    public static class ClientHandler implements Runnable {

        private Socket clientSocket;
        private BufferedReader fromClient;
        private PrintStream toClient;
        private Map<String, Integer> users;

		ClientHandler(Socket socket, Map<String, Integer> users) {
			this.clientSocket = socket;
            this.users = users;

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

            try {
                String line = fromClient.readLine();
                registerUser(line);

                line = fromClient.readLine();

                while (line != null) {

                    String arg1 = line.split(" ")[0];

                    switch (arg1) {
                        case "*play":
                            String arg2 = line.split(" ")[1];
                            String arg3 = line.split(" ")[2];
                            toClient.println("Playing " + arg2 + " words with a failed attempt factor of " + arg3);
                            this.playPuzzle();
                            break;
                        
                        case "*stats":
                            System.out.println("Stats requested");
                            break;

                        case "*exit":
                            System.out.println("Exit requested");
                            break;
                    }
                    
                    line = fromClient.readLine();
                }

                clientSocket.close();
                System.out.println("Closed: " + clientSocket);

            } catch (IOException e) {
                System.out.println(e);
                System.exit(1);
            }

        }

        private void registerUser(String line) {

            String arg2 = line.split(" ")[1];

            if (users.containsKey(arg2)) {
                toClient.println("Welcome back " + arg2);
                System.out.println(arg2 + " already in database");
            } else {
                users.put(arg2, 0);
                toClient.println("Welcome " + arg2);
                System.out.println("added " + arg2 + " to database");
            }
        }

        private void playPuzzle() {

            System.out.println("playing puzzle");
            toClient.println("1-----+2-----+3-----+");
        }

    }
}
