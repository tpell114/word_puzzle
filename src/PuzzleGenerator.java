import java.util.*;

public class PuzzleGenerator {

    // Method to generate the initial criss-cross puzzle
    public static char[][] createInitialPuzzle(List<String> words, int puzzleSize) {
        char[][] puzzle = new char[puzzleSize][puzzleSize];
        
        // Initialize the puzzle with empty spaces
        for (int i = 0; i < puzzleSize; i++) {
            Arrays.fill(puzzle[i], '.');
        }
        
        // Pick a random word as the vertical stem
        Random random = new Random();
        String verticalStem = words.get(random.nextInt(words.size()));
        int stemStartRow = (puzzleSize - verticalStem.length()) / 2;
        int stemCol = puzzleSize / 2;

        // Place the vertical stem
        for (int i = 0; i < verticalStem.length(); i++) {
            puzzle[stemStartRow + i][stemCol] = verticalStem.charAt(i);
        }

        // Add horizontal words that intersect with the vertical stem
        for (int i = 0; i < verticalStem.length(); i++) {
            char intersectChar = verticalStem.charAt(i);
            List<String> validWords = getWordsContainingChar(words, intersectChar);
            if (!validWords.isEmpty()) {
                String horizontalWord = validWords.get(random.nextInt(validWords.size()));
                int startCol = Math.max(0, stemCol - horizontalWord.indexOf(intersectChar));
                int row = stemStartRow + i;

                for (int j = 0; j < horizontalWord.length(); j++) {
                    int col = startCol + j;
                    if (col < puzzleSize && puzzle[row][col] == '.') {
                        puzzle[row][col] = horizontalWord.charAt(j);
                    }
                }
            }
        }
        
        return puzzle;
    }

    // Helper method to get words containing a specific character
    private static List<String> getWordsContainingChar(List<String> words, char c) {
        List<String> result = new ArrayList<>();
        for (String word : words) {
            if (word.indexOf(c) != -1) {
                result.add(word);
            }
        }
        return result;
    }

    // Method to display the puzzle
    public static void displayPuzzle(char[][] puzzle) {
        for (char[] row : puzzle) {
            for (char ch : row) {
                System.out.print(ch);
            }
            System.out.println();
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        List<String> wordList = Arrays.asList("DISTRIBUTED", "SYSTEM", "RELIABLE", "DESIGN");
        char[][] puzzle = createInitialPuzzle(wordList, 15);
        displayPuzzle(puzzle);
    }
}
