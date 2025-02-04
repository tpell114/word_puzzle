import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PuzzleObject {

    private int numWords;
    private int difficultyFactor;
    private int guessCounter;
    private String stem;
    private List<String> horizontalWords = new ArrayList<>();
    private char[][] puzzleMaster;
    private char[][] puzzleSlave;

    PuzzleObject(int numWords, int difficultyFactor) throws Exception{
        this.numWords = numWords;
        this.difficultyFactor = difficultyFactor;
        this.guessCounter = 0;
        this.stem = contactWordRepository(Constants.CMD_GET_STEM_WORD, String.valueOf((numWords-1)*2));

        if (stem.equals("ERROR")){
            System.out.println("error with word repo, service might not be running");
            throw new RuntimeException("error with word repo, service might not be running");
        }

        this.guessCounter += stem.length() * difficultyFactor;

        for (int i = 0; i < stem.length(); i += 2) {
            String word = contactWordRepository(Constants.CMD_GET_RANDOM_WORD, String.valueOf(stem.charAt(i)));
            horizontalWords.add(word);
            this.guessCounter += word.length() * difficultyFactor;
            if (horizontalWords.size() == numWords - 1) break;
        }

        this.initPuzzleMaster();
        this.initPuzzleSlave();
    }


    /**
     * Initializes the puzzleMaster 2D array with the given stem and horizontal words.
     * The puzzleMaster is a 2D array of characters, where each row represents a line
     * in the puzzle and each column represents a letter in the puzzle.
     * The puzzleMaster is initialized such that each row has enough columns to fit the
     * longest horizontal word, and each column is initialized with a '.' character.
     * The stem is then placed vertically in the middle of the puzzleMaster, and each
     * horizontal word is placed at the correct position in the puzzleMaster such that
     * the intersecting letter of the horizontal word and the stem lines up.
     */
    private void initPuzzleMaster(){

        System.out.println(stem);
        System.out.println(horizontalWords);

        int ySize = stem.length();

        String longest = null;
        for (String word : horizontalWords) {
            if (longest == null || word.length() > longest.length()) {
                longest = word;
            }
        }

        int xSize = longest.length() * 2;
        this.puzzleMaster = new char[ySize][xSize];

        for (int i = 0; i < ySize; i++) {
            for (int j = 0; j < xSize; j++) {
                puzzleMaster[i][j] = '.';
            }
        }

        for (int i = 0; i < ySize; i++) {
            puzzleMaster[i][xSize/2] = stem.charAt(i);
        }

        int stemIndex = 0;
        for (int i = 0; i < horizontalWords.size(); i++) {

            String word = horizontalWords.get(i);
            char intersectChar = stem.charAt(stemIndex);
            int offset = word.indexOf(intersectChar);
            int startColumn = xSize/2 - offset;

            for (int j = 0; j < word.length(); j++) {
                puzzleMaster[stemIndex][startColumn + j] = word.charAt(j);
            }

            stemIndex += 2;
        }
     

        for (char[] row : puzzleMaster) {
            System.out.println(new String(row));
        }

    }

    /**
     * Initializes the puzzleSlave 2D array with the same dimensions as the
     * puzzleMaster. The puzzleSlave is initialized such that any '.' character
     * in the puzzleMaster is replaced with a '.' character in the puzzleSlave
     * and any other character in the puzzleMaster is replaced with a '-'
     * character in the puzzleSlave.
     */
    private void initPuzzleSlave(){

        puzzleSlave = new char[puzzleMaster.length][puzzleMaster[0].length];

        for (int i = 0; i < puzzleSlave.length; i++) {
            for (int j = 0; j < puzzleSlave[i].length; j++) {
                if (puzzleMaster[i][j] == '.') {
                    puzzleSlave[i][j] = '.';
                } else {
                    puzzleSlave[i][j] = '-';
                }
            }
        }

        for (char[] row : puzzleSlave) {
            System.out.println(new String(row));
        }
        
    }

    /**
     * Constructs a string representation of the puzzleSlave grid.
     * Each row of the puzzleSlave grid is concatenated into a string,
     * with rows separated by a '+' character. Additionally, appends
     * the current guess counter to the end of the string.
     *
     * @return A string representation of the puzzleSlave grid with 
     *         the guess counter appended.
     */
    public String getPuzzleSlaveString(){
        String returnString = "";
        for (char[] row : puzzleSlave) {
            returnString += new String(row) + "+";
        }
        returnString += "Counter: " + this.guessCounter + "+";
        return returnString;
    }

    /**
     * Processes a character guess in the puzzle.
     * Decrements the guess counter and checks if the guessed character
     * is present in the puzzleMaster grid. If found, updates the corresponding
     * positions in the puzzleSlave grid with the guessed character.
     * 
     * @param guess The character guessed by the player.
     * @return true if the puzzleSlave matches the puzzleMaster after the guess,
     *         indicating the puzzle is solved; otherwise, returns false.
     */
    public Boolean guessChar(char guess){

        System.out.println("Guessing " + guess);
        this.guessCounter--;

        for (int i = 0; i < puzzleMaster.length; i++) {
            for (int j = 0; j < puzzleMaster[i].length; j++) {
                if (puzzleMaster[i][j] == guess) {
                    System.out.println("Found " + guess + " at (" + i + ", " + j + ")");
                    puzzleSlave[i][j] = guess;
                }
            }
        }

        if (Arrays.deepEquals(puzzleSlave, puzzleMaster)) {
            return true;
        }
        return false;
    }

    /**
     * Processes a word guess in the puzzle.
     * Decrements the guess counter and checks if the guessed word
     * is either the stem word or one of the horizontal words in the puzzleMaster grid.
     * If found, updates the corresponding positions in the puzzleSlave grid with the
     * characters of the guessed word.
     * 
     * @param guess The word guessed by the player.
     * @return true if the puzzleSlave matches the puzzleMaster after the guess,
     *         indicating the puzzle is solved; otherwise, returns false.
     */
    public Boolean guessWord(String guess){

        System.out.println("Guessing " + guess);
        this.guessCounter--;

        if (guess.equals(this.stem)) {
            for (int i = 0; i < puzzleMaster.length; i++) {
                puzzleSlave[i][puzzleMaster[i].length/2] = stem.charAt(i);
            }
        } else if (horizontalWords.contains(guess)) {

            System.out.println("Found " + guess);

            for (int i = 0; i < puzzleMaster.length; i += 2) {

                String line = "";

                for (int j = 0; j < puzzleMaster[i].length; j++) {
                    line += puzzleMaster[i][j];
                }

                if (line.contains(guess)) {
                    for (int j = 0; j < puzzleMaster[i].length; j++) {
                        puzzleSlave[i][j] = puzzleMaster[i][j];
                    }
                }
            }

        }


        if (Arrays.deepEquals(puzzleSlave, puzzleMaster)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the current guess counter of the puzzle object.
     * 
     * @return The current guess counter.
     */
    public int getGuessCounter(){
        return this.guessCounter;
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
    private static String contactWordRepository(String cmdCode, String message){
            
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
