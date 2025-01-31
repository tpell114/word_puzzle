import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class WordRepoMicroservice 
{
    private static final int PORT = 9090;

    private List<String> words;

    public static void main(String[] args)
    {
        try{
            WordRepoMicroservice wordRepo = new WordRepoMicroservice("words.txt");
            wordRepo.runUDPServer();
        }
        catch(Exception e)
        {
            System.err.println("Error creating repo object with filepath" + e.getMessage());
            e.printStackTrace();
        }
    }

    public WordRepoMicroservice(String filepath)
    {
            words = new ArrayList<>();
            loadWords(filepath);
    }

    private void loadWords(String filepath)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath)))
        {
            Pattern pattern = Pattern.compile("^[A-Za-z]+$");

            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();

                if(pattern.matcher(line).matches())
                {
                words.add(line.toLowerCase());
                }
            }
            updateWordFile("words.txt");

        }
        catch(Exception e)
        {
            System.err.println("Error loading words: " + e.getMessage());
        }
    }

    public boolean wordExists(String word)
    {
        return binarySearch(words, word.toLowerCase());
    }

    private boolean binarySearch(List<String> sortedWords, String target)
    {
        int left = 0;
        int right = sortedWords.size() - 1;

        while(left <= right)
        {
            int mid = left + (right - left) / 2;
            int comparison = sortedWords.get(mid).compareTo(target);
            if(comparison == 0)
            {
                return true;
            }
            else if (comparison < 0)
            {
                left = mid + 1;
            }
            else
            {
                right = mid - 1;
            }
        }

        return false;

    }

    public boolean addWord(String word) 
    {
        word = word.toLowerCase().trim();

        if (!word.matches("^[a-z]+$")) {
            System.out.println(word + " is NOT added because it contains non-alphabetic characters.");
            return false;
        }

        boolean exists = binarySearch(words, word);

        if (!exists) 
        {
            int position = findInsertPosition(word);

            words.add(position, word);
            System.out.println(word + " has been added");
            updateWordFile("words.txt");
            return true;
           
        }
        System.out.println(word + " has NOT been added");
        return false;
    }


    public boolean removeWord(String word) 
    {
        word = word.toLowerCase();

        boolean exists = binarySearch(words, word);

        if (exists) 
        {
            int position = findWordIndex(words, word);
            if(position != -1)
            {
            words.remove(position);
            System.out.println(word + " has been removed");
            updateWordFile("words.txt");
            return true;
            }
        }
        System.out.println(word + " has NOT been removed as it does not exist in the word file");
        return false;
    }

    //private String[] words(int numOfWords)
  //  {

  //  }

    private int findInsertPosition(String word)
    {
        int left = 0;
        int right = words.size();

        while(left < right)
        {
            int mid = left + (right - left) / 2;
            if (words.get(mid).compareTo(word) < 0)
            {
                left = mid + 1;
            }
            else
            {
                right = mid;
            }
        }
        return left;
    }

    private int findWordIndex(List<String> sortedWords, String target)
    {
        int left = 0;
        int right = sortedWords.size() - 1;

        while(left <= right)
        {
            int mid = left + (right - left) / 2;
            int comparison = sortedWords.get(mid).compareTo(target);
            if(comparison == 0)
            {
                return mid;
            }
            else if (comparison < 0)
            {
                left = mid + 1;
            }
            else
            {
                right = mid - 1;
            }
        }

        return -1;

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
                System.out.println("Recieved: " + message);


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

    private String convertBoolToString(boolean response)
    {
        if(response == true)
        {
            return "true";
        }
        else
        return "false";
    }
    

    private String processRequest(String message)
    {
        byte cmdCode;
        String contents;
        String[] parts = message.split(" ", 2);
        boolean isTrue;
        try
        {
            cmdCode = (byte) Integer.parseInt(parts[0], 16);  
            contents = (parts[1]);

            switch (cmdCode)
            {
                case ProtocolConstants.CMD_CHECK_IF_WORD_EXISTS:
                isTrue = wordExists(contents);
                return convertBoolToString(isTrue);

                case ProtocolConstants.CMD_ADD_WORD:
                isTrue = addWord(contents);
                return convertBoolToString(isTrue);

                case ProtocolConstants.CMD_REMOVE_WORD:
                isTrue = removeWord(contents);
                return convertBoolToString(isTrue);

                case ProtocolConstants.CMD_GET_STEM_WORD:
                String stem = getStemWord(contents);
                

                default:
                System.out.println("Recieved unrecognized command and message: " + cmdCode + " " + message);
                return "Recieved unrecognized command and message: " + cmdCode + " " + message;
              //  out.println("ERROR: Unrecognized command and message:" + cmdCode + " " + message) 
            }

        }
        catch (Exception e)
        {
            System.out.println("Invalid cmd format" + parts[0]);
            return "Invalid cmd format" + parts[0];
        }
    }

    private String getStemWord(String constraints)
    {
        String stem;


        return stem;
    }

    private void updateWordFile(String filePath)
    {
        try(PrintWriter writer = new PrintWriter(new FileWriter(filePath)))
        {
            for(String w : words)
            {
                writer.println(w);
            }
        }
            catch(IOException e)
            {
                System.err.println("Error writing words to disk: " + e.getMessage());
            }
    }
}















