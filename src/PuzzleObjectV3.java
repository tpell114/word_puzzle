import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class PuzzleObjectV3 {

    private int numWords;
    private int difficultyFactor;
    private String stem;
    private List<String> horizontalWords = new ArrayList<>();
    private char[][] puzzle;


    PuzzleObjectV3(int numWords, int difficultyFactor){
        this.numWords = numWords;
        this.difficultyFactor = difficultyFactor;
        this.stem = contactWordRepository(ProtocolConstantsV2.CMD_GET_STEM_WORD, String.valueOf((numWords-1)*2));

        for (int i = 0; i < stem.length(); i += 2) {
            String word = contactWordRepository(ProtocolConstantsV2.CMD_GET_RANDOM_WORD, String.valueOf(stem.charAt(i)));
            horizontalWords.add(word);
            if (horizontalWords.size() == numWords - 1) break;
        }

        this.initPuzzle();
    }


    private void initPuzzle(){

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
        this.puzzle = new char[ySize][xSize];

        for (int i = 0; i < ySize; i++) {
            for (int j = 0; j < xSize; j++) {
                puzzle[i][j] = '-';
            }
        }

        //for (int i = 0; i < ySize; i++) {
        //    puzzle[i][xSize/2] = stem.charAt(i);
        //}
/* 
        for (int i = 0; i < horizontalWords.size(); i++) {

            String word = horizontalWords.get(i);
            int stemIndex = 0;
            char intersectChar = stem.charAt(i);

            
        }
   */     

        for (char[] row : puzzle) {
            System.out.println(new String(row));
        }

    }





    private static String contactWordRepository(String cmdCode, String message){
            
        try(DatagramSocket socket = new DatagramSocket()){
            InetAddress address = InetAddress.getByName("localhost");
        
            String fullMessage = cmdCode + " " + message + ProtocolConstantsV2.MSG_TERMINATOR;
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
