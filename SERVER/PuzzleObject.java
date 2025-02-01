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
            randomWord = PuzzleServer.getRandomWord();
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
       
}

