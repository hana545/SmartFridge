package hr.riteh.sl.smartfridge.FirebaseDatabase;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

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

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("grocery_name", grocery_name);
        result.put("quantity", quantity);
        result.put("exp_date", exp_date);
        return result;
    }


}
