public class ProtocolConstantsV2 {

   //client to server
   public static final String CMD_EXIT = "01";
   public static final String CMD_SIGN_IN = "02";
   public static final String CMD_LEVEL_SET = "03";
   public static final String CMD_SUBMIT_GUESS = "04";
   public static final String CMD_CHECK_SCORE = "05";
   public static final String CMD_REQ_NEW_GAME = "06";
   public static final String CMD_ABORT_GAME = "07";

   //client to server && server to wordrepo
   public static final String CMD_CHECK_IF_WORD_EXISTS = "08";
   public static final String CMD_ADD_WORD = "09";
   public static final String CMD_REMOVE_WORD = "10";

    //server to client
    public static final String CMD_SND_WELCOME = "11";
    public static final String CMD_SND_EMPTY_PUZZLE = "12";
    public static final String CMD_SND_PUZZLE = "13";
    public static final String CMD_SND_COUNTER = "14";
    public static final String CMD_SND_SCORE = "15";
    public static final String CMD_SND_GAMEWIN = "16";
    public static final String CMD_SND_GAMELOSS = "17";
    public static final String CMD_SND_FAIL_ATTEMPT = "18";
    public static final String CMD_SND_MISCELLANEOUS= "19"; 

    //server to word repo
    public static final String CMD_GET_RANDOM_WORD = "20";
    public static final String CMD_GET_STEM_WORD = "21";
  


    // Message format constants
    public static final String MSG_TERMINATOR = "\n";
    public static final int MAX_CONTENT_LEN = 128;
    public static final int MAX_USERNAME_LEN = 32;
    public static final int MAX_MESSAGE_LEN_TO_SERVER = 132;
    public static final int MAX_MESSAGE_LEN_FROM_SERVER = 256;

    // Private constructor to prevent instantiation
    private ProtocolConstantsV2() {
        // This class should not be instantiated
    }
}