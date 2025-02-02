import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordRepoMicroserviceV3 {

    private static final int PORT = 9090;
    private static List<String> words = new ArrayList<>();

    public WordRepoMicroserviceV3(String filepath){

        this.loadWords(filepath);
    }


    public static void main(String[] args) {
        
        WordRepoMicroserviceV3 wordRepo = new WordRepoMicroserviceV3("words.txt");
        wordRepo.runUDPServer();

    }

    private void runUDPServer()
    {
        try(DatagramSocket socket = new DatagramSocket(PORT))
        {
            System.out.println("WordRepoMicroservice is running on port " + PORT);
            byte[] buffer = new byte[1024];
            while(true)
            {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String message = new String(request.getData(), 0, request.getLength());
                System.out.println("Received: " + message);
                String response = processRequest(message);
                byte[] responseBytes = response.getBytes();
                DatagramPacket reply = new DatagramPacket(responseBytes, responseBytes.length, request.getAddress(), request.getPort());
                socket.send(reply);
            }
        }
        catch(IOException e)
        {
            System.err.println("Error running UDP server: " + e.getMessage());
        }
    }

    private void loadWords(String filepath) {

        System.out.println("Loading words from file: " + filepath);

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim());
            }
            System.out.println("Loaded " + words.size() + " words.");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private String processRequest(String message){

        String cmdCode = message.split(" ")[0];
        String contents = message.split(" ")[1].trim();

        switch (cmdCode) {

            case ProtocolConstantsV2.CMD_GET_STEM_WORD:
                System.out.println("Getting stem word with minimum length: " + contents);
                String stemWord = getWord(Integer.valueOf(contents));
                System.out.println("Returning stem word: " + stemWord);
                return stemWord;
            
            case ProtocolConstantsV2.CMD_GET_RANDOM_WORD:
                System.out.println("Getting random word that contains character: " + contents);
                String randomWord = getWord(contents);
                System.out.println("Returning random word: " + randomWord);
                return randomWord;

        }
        return null;
    }

    private String getWord(int minLength){

        Random random = new Random();
        int index = random.nextInt(words.size());
        String word = words.get(index);
        while(word.length() < minLength){
            index = random.nextInt(words.size());
            word = words.get(index);
        }
        return word;
    }

    private String getWord(String contains){

        Random random = new Random();
        int index = random.nextInt(words.size());
        String word = words.get(index);
        while(!word.contains(String.valueOf(contains.toLowerCase()))){
            index = random.nextInt(words.size());
            word = words.get(index);
        }
        return word;
    }



}











