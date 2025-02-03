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

    PuzzleObject(int numWords, int difficultyFactor){
        this.numWords = numWords;
        this.difficultyFactor = difficultyFactor;
        this.guessCounter = 0;
        this.stem = contactWordRepository(Constants.CMD_GET_STEM_WORD, String.valueOf((numWords-1)*2));
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

    public String getPuzzleSlaveString(){
        String returnString = "";
        for (char[] row : puzzleSlave) {
            returnString += new String(row) + "+";
        }
        returnString += "Counter: " + this.guessCounter + "+";
        return returnString;
    }

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

    public int getGuessCounter(){
        return this.guessCounter;
    }


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
