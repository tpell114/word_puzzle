public class Constants {

   //client to server
   public static final String CMD_EXIT = "01";
   public static final String CMD_SIGN_IN = "02";
   public static final String CMD_LEVEL_SET = "03";
   public static final String CMD_SUBMIT_GUESS = "04";
   public static final String CMD_CHECK_SCORE = "05";
   public static final String CMD_ABORT_GAME = "06";

   //client to server && server to wordrepo
   public static final String CMD_CHECK_IF_WORD_EXISTS = "07";
   public static final String CMD_ADD_WORD = "08";
   public static final String CMD_REMOVE_WORD = "09";

    //server to client
    public static final String CMD_SND_PUZZLE = "10";
    public static final String CMD_SND_SCORE = "11";
    public static final String CMD_SND_GAMEWIN = "12";
    public static final String CMD_SND_GAMELOSS = "13";
    public static final String CMD_SND_MISCELLANEOUS= "14"; 
    public static final String CMD_SND_ERROR = "99";

    //server to word repo
    public static final String CMD_GET_RANDOM_WORD = "15";
    public static final String CMD_GET_STEM_WORD = "16";

    // Message format constants
    public static final String MSG_TERMINATOR = "\n";

    // Message constants
    public static final String MAIN_MENU_MESSAGE = "\nSelect from the following options:\n"
                                                    +"1. Play a new game\n"
                                                    +"2. View statistics\n"
                                                    +"3. Modify word repository\n"
                                                    +"4. Exit\n";

    public static final String USER_SIGN_IN_MESSAGE = "\nWelcome to Word Puzzle!\n"
                                                        +"=======================\n"
                                                        +"Please enter your name:\n";

    public static final String GUESS_MESSAGE = "\nPlease guess a letter or a word (enter ~ to return to menu):"
                                                + "you can also verify if a word exists by prefixing a word with '?' eg. ?apple\n";

    public static final String WORD_REPO_MESSAGE = "\nAdd words to the repo by prefixing a word with '+'  eg. +apple\n"
                                                    + "remove words from the repo by prefixing a word with '-' eg. -apple\n"
                                                    + "check if a word exists by prefixing a word with '?' eg. ?apple\n"
                                                    + "enter '~' to return to menu";
                                                    
    private Constants() {
        // This class should not be instantiated
    }
}