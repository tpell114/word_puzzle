public class ProtocolConstants {

   //client to server
   public static final byte CMD_EXIT = 0x01;
   public static final byte CMD_SIGN_IN = 0x02;
   public static final byte CMD_LEVEL_SET = 0x03;
   public static final byte CMD_SUBMIT_GUESS = 0x04;
   public static final byte CMD_CHECK_SCORE = 0x05;
   public static final byte CMD_REQ_NEW_GAME = 0x06;
   public static final byte CMD_ABORT_GAME = 0x07;
   public static final byte CMD_CHECK_IF_WORD_EXISTS = 0x08;
   public static final byte CMD_ADD_WORD = 0x09;
   public static final byte CMD_REMOVE_WORD = 0x0A;

    //server to client
    public static final byte CMD_SND_WELCOME = 0x11;
    public static final byte CMD_SND_EMPTY_PUZZLE = 0x12;
    public static final byte CMD_SND_PUZZLE = 0x13;
    public static final byte CMD_SND_COUNTER = 0x14;
    public static final byte CMD_SND_SCORE = 0x15;
    public static final byte CMD_SND_ENDGAME = 0x16;
    public static final byte CMD_SND_FAIL_ATTEMPT = 0x17;
    public static final byte CMD_SND_Mischellaneous= 0x18;

    // Message format constants
    public static final String MSG_TERMINATOR = "\n";
    public static final int MAX_CONTENT_LEN = 128;
    public static final int MAX_USERNAME_LEN = 32;
    public static final int MAX_MESSAGE_LEN_TO_SERVER = 132;
    public static final int MAX_MESSAGE_LEN_FROM_SERVER = 256;

    // Private constructor to prevent instantiation
    private ProtocolConstants() {
        // This class should not be instantiated
    }
}