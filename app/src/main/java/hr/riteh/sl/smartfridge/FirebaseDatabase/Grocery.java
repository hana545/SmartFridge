package hr.riteh.sl.smartfridge.FirebaseDatabase;

public class Grocery {
    public String ownerID;
    public String fridgeID;
    public String grocery_name;
    public String quantity;
    public String exp_date;

    public Grocery() {

    }

    public Grocery(String ownerID, String fridgeID, String grocery_name, String quantity, String exp_date) {
        this.ownerID = ownerID;
        this.fridgeID = fridgeID;
        this.grocery_name = grocery_name;
        this.quantity = quantity;
        this.exp_date = exp_date;
    }


}
