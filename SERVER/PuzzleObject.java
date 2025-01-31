import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

public class PuzzleObject 
{

    String stemWord;
    String horizontalWords;
    int numOfWords;

    public PuzzleObject(int numOfWords)
    {
        ;

    }


    public static String displayPuzzle(ArrayList<String> wordList, String stem) 
    {
        String puzzle;
        ArrayList<String> puzzleStructure = new ArrayList<>();
        boolean hasHorizontalWord = false;
        boolean horizontalWordDirectlyAbove = false;
        int i;
        int j;
        int count = 0;

        stemLoop: for (i = 0; i < stem.length(); i++) 
        {
            if(horizontalWordDirectlyAbove == true || wordList.isEmpty() == true)
            {
                puzzleStructure.add(String.valueOf(stem.charAt(i)));
                        System.out.println("\nput " + stem.charAt(i) + " in the array"); 
                        horizontalWordDirectlyAbove = false;
                        continue stemLoop;
            }
           count = 0;
            System.out.println("\n\ni = " + i+ " char is: " + stem.charAt(i));
            for (String horizWord : wordList) 
            {
                System.out.println("count = " + count + " word is " + horizWord);
                for (j = 0; j < horizWord.length(); j++) 
                {
                    System.out.println("j = " + j + " char is: " + horizWord.charAt(j));
                    System.out.println("horizontal above = " + horizontalWordDirectlyAbove);

                    if (horizWord.charAt(j) == stem.charAt(i) && horizontalWordDirectlyAbove == false) 
                    {
                        puzzleStructure.add(horizWord);
                        System.out.println("\nput " + horizWord + " in the array"); 
                        wordList.remove(horizWord);
                        horizontalWordDirectlyAbove = true;
                        hasHorizontalWord = true;
                        count ++;
                        continue stemLoop;
                    }

                    if (!hasHorizontalWord && count == horizWord.length() -1) 
                    {
                        puzzleStructure.add(String.valueOf(stem.charAt(i)));
                        System.out.println("\nput " + stem.charAt(i) + " in the array"); 
                        horizontalWordDirectlyAbove = false;
                        continue stemLoop;


                    }

                    

                    
                   

                }
                count ++;


            }

        }
        puzzle = "hey";
        System.out.println(puzzleStructure);

        return puzzle;
    }

}