import java.io.*;
import java.net.*;
import java.util.*;

public class PuzzleObjectV2 {

    String stemWord;
    String horizontalWords;
    int numOfWords;

    public static ArrayList<String> generatePuzzle(String stem, String numOfWords) {
        ArrayList<String> puzzleStructure = new ArrayList<>();
        boolean horizontalWordDirectlyAbove = false;
        int i;
        int j;
        int horizontalNumOfWords = 0;
        int intNumOfWords = Integer.parseInt(numOfWords);
        String randomWord;
        int stemIndex = 0;
        boolean added = false;

        findWordLoop: while (horizontalNumOfWords < intNumOfWords - 1) {
            randomWord = contactWordRepository(ProtocolConstantsV2.CMD_GET_RANDOM_WORD, "\0");
            added = false;

            stemLoop: for (i = stemIndex; i < stem.length(); i++) {
                if (horizontalWordDirectlyAbove == true) {
                    puzzleStructure.add(String.valueOf(stem.charAt(i)));
                    horizontalWordDirectlyAbove = false;
                    stemIndex++;
                    continue stemLoop;
                }
                for (j = 0; j < randomWord.length(); j++) {
                    if (randomWord.charAt(j) == stem.charAt(i) && horizontalWordDirectlyAbove == false) {
                        puzzleStructure.add(randomWord);
                        added = true;
                        horizontalNumOfWords++;
                        horizontalWordDirectlyAbove = true;
                        stemIndex++;
                        continue findWordLoop;
                    }
                }
                if(added == false)
                {
                    continue findWordLoop;
                }
            }
        }

        while (stemIndex < stem.length()) {
            puzzleStructure.add(String.valueOf(stem.charAt(stemIndex)));
            stemIndex++;
        }

        
        System.out.println("Puzzle struct is : " + puzzleStructure);
        return puzzleStructure;
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
        catch (IOException e) 
        {
            System.err.println("Error communicating with WordRepoMicroservice: " + e.getMessage());
            return "ERROR"; 
        }

    }
       
}

