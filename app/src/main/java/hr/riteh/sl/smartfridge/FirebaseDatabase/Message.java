package hr.riteh.sl.smartfridge.FirebaseDatabase;

public class Message {
    public String text, author, fridgeID;

    public Message() {
    }

    public Message(String text, String author, String fridgeID) {
        this.text = text;
        this.author = author;
        this.fridgeID = fridgeID;
    }
}
