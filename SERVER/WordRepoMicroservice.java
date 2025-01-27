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
            return true;
        }
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

}













