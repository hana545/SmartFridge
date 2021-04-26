package hr.riteh.sl.smartfridge.FirebaseDatabase;

public class Message {
    public String text, authorID, fridgeID;

    public Message() {
    }

    public Message(String text, String authorID, String fridgeID) {
        this.text = text;
        this.authorID = authorID;
        this.fridgeID = fridgeID;
    }
}
