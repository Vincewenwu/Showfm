package co.bankoo.zuweie.showfm.model;

public class Msg {
    private static int next_msg_id = 1;
    private static int makeMsgId() {
        return next_msg_id++;
    }

    public static int PLAY     = makeMsgId();
    public static int PAUSE    = makeMsgId();
    public static int SEEKTO   = makeMsgId();
    public static int PLAY_LOCAL = makeMsgId();
}