import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WordRepoMicroservice {

    private static final int PORT = 9090;
    private static List<String> words = new ArrayList<>();

    public WordRepoMicroservice(String filepath){
        this.loadWords(filepath);
    }


    public static void main(String[] args) {
        WordRepoMicroservice wordRepo = new WordRepoMicroservice("words.txt");
        wordRepo.runUDPServer();
    }

    private void runUDPServer()
    {
        try(DatagramSocket socket = new DatagramSocket(PORT)){

            System.out.println("WordRepoMicroservice is running on port " + PORT);
            byte[] buffer = new byte[1024];

            while(true){
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
        catch(IOException e){
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

            case Constants.CMD_GET_STEM_WORD:
                System.out.println("Getting stem word with minimum length: " + contents);
                String stemWord = getWord(Integer.valueOf(contents));
                System.out.println("Returning stem word: " + stemWord);
                return stemWord;
            
            case Constants.CMD_GET_RANDOM_WORD:
                System.out.println("Getting random word that contains character: " + contents);
                String randomWord = getWord(contents);
                System.out.println("Returning random word: " + randomWord);
                return randomWord;

            case Constants.CMD_CHECK_IF_WORD_EXISTS:
                System.out.println("Checking if word exists: " + contents);
                return this.handleWordExists(contents);

            case Constants.CMD_ADD_WORD:
                System.out.println("Adding word: " + contents);
                return this.handleWordAdd(contents);

            case Constants.CMD_REMOVE_WORD:
                System.out.println("Removing word: " + contents);
                return this.handleWordRemove(contents);
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

    private String handleWordExists(String word){

        if (words.contains(word.toLowerCase())) {
            System.out.println("Word exists: " + word);
            return "1";
        }

        System.out.println("Word does not exist: " + word);

        return "0";
    }

    private String handleWordAdd(String word){

       int index = Collections.binarySearch(words, word.toLowerCase());

       if (index < 0) {
            words.add(-index - 1, word.toLowerCase());
            System.out.println("Word added: " + word);
            return "1";
        }

        System.out.println("Word already exists: " + word);

        return "0";
    }

    private String handleWordRemove(String word){

        int index = Collections.binarySearch(words, word.toLowerCase());

        if (index >= 0) {
            words.remove(index);
            System.out.println("Word removed: " + word);
            return "1";
        }

        System.out.println("Word does not exist: " + word);
        return "0";
    }

}











