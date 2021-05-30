package hr.riteh.sl.smartfridge.FirebaseDatabase;

public class Message {
    public String text, author, authorID, fridgeID;

    public Message() {
    }

    public Message(String text, String author, String authorID, String fridgeID) {
        this.text = text;
        this.author = author;
        this.authorID = authorID;
        this.fridgeID = fridgeID;
    }
}
