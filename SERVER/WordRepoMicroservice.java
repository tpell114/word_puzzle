import java.net.*;
import java.io.*;
import java.util.*;

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
            String line;
            while ((line = reader.readLine()) != null)
            {
                words.add(line.trim());
            }

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
        word = word.toLowerCase();

        boolean exists = binarySearch(words, word);

        if (!exists) 
        {
            int position = findInsertPosition(word);

            words.add(position, word);
            System.out.println(word + " has been added");
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
            return true;
            }
        }
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

               boolean responseBool = processRequest(message);
               String responseString;

               if (responseBool == true)
               {
               responseString = "true";
               }
               else
               { 
               responseString = "false";
               }

                byte[] responseBytes = responseString.getBytes();

                DatagramPacket reply = new DatagramPacket(responseBytes, responseBytes.length, request.getAddress(), request.getPort());
                socket.send(reply);
            }
            

        }
        catch(IOException e)
        {
            System.err.println("Error running UDP server: " + e.getMessage());
        }
    }

    private boolean processRequest(String message)
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
                return wordExists(contents);

                case ProtocolConstants.CMD_ADD_WORD:
                return addWord(contents);

                case ProtocolConstants.CMD_REMOVE_WORD:
                return removeWord(contents);

                default:
                System.out.println("Recieved unrecognized command and message: " + cmdCode + " " + message);
                return false;
              //  out.println("ERROR: Unrecognized command and message:" + cmdCode + " " + message) 
            }

        }
        catch (Exception e)
        {
            System.out.println("Invalid cmd format" + parts[0]);
            return false; //need to handle this error in run()
        }
    }

}













