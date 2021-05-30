package hr.riteh.sl.smartfridge.FirebaseDatabase;

public class MyFridge {
    public String name;
    public String ownerID;
    public Boolean primary;

    public MyFridge() {

    }
    public MyFridge(String name, String ownerID, Boolean primary) {
        this.name = name;
        this.ownerID = ownerID;
        this.primary = primary;
    }
}
