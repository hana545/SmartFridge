package hr.riteh.sl.smartfridge.FirebaseDatabase;

public class Fridge {
    public String name;
    public String ownerID;
    public Boolean primary;

    public Fridge() {

    }
    public Fridge(String name, String ownerID, Boolean primary) {
        this.name = name;
        this.ownerID = ownerID;
        this.primary = primary;
    }
}
