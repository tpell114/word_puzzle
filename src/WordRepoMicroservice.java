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

    /**
     * Runs a UDP server that listens for incoming datagram packets on a specified port.
     * Continuously receives messages from clients, processes each message, and sends
     * a response back to the client. Handles any IOExceptions that occur during
     * the operation of the server.
     */
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

    /**
     * Loads a list of words from a specified file and stores them in memory.
     * The file is expected to have one word per line. Prints the number of words
     * loaded to the console. If there is an error reading the file, prints the
     * error message to the console.
     * @param filepath the name of the file to load words from
     */
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

    /**
     * Processes a request received from a client via UDP. The request is a string
     * in the format "<command code> <contents>" where <command code> is a string
     * that specifies the type of operation to be performed and <contents> is a
     * string that contains any additional information needed to perform the
     * operation. The method performs the requested operation and returns a string
     * containing the result of the operation.
     * 
     * @param message the request received from the client
     * @return the result of the requested operation
     */
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

    /**
     * Returns a word from the word repository of length at least minLength.
     * 
     * @param minLength the minimum length of the word to be returned
     * @return a word from the word repository of length at least minLength
     */
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

    /**
     * Returns a word from the word repository that contains the given character.
     * The word is randomly chosen from the repository.
     * 
     * @param contains the character to search for in the word
     * @return a word from the word repository that contains the given character
     */
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

    /**
     * Checks if a given word exists in the word repository.
     * 
     * @param word the word to check
     * @return "1" if the word exists, "0" otherwise
     */
    private String handleWordExists(String word){

        if (words.contains(word.toLowerCase())) {
            System.out.println("Word exists: " + word);
            return "1";
        }

        System.out.println("Word does not exist: " + word);

        return "0";
    }

    /**
     * Adds a word to the word repository if it does not already exist.
     * 
     * @param word the word to add
     * @return "1" if the word was added, "0" otherwise
     */
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

    /**
     * Removes a word from the word repository if it exists.
     * 
     * @param word the word to remove
     * @return "1" if the word was removed, "0" otherwise
     */
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
