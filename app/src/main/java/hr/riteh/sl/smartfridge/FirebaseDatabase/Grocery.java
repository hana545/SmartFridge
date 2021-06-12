package hr.riteh.sl.smartfridge.FirebaseDatabase;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Grocery {
    public String ownerID;
    public String name;
    public Integer quantity;
    public String unit;
    public String exp_date;

    public Grocery() {

    }

    public Grocery(String ownerID, String grocery_name, Integer quantity, String unit, String exp_date) {
        this.ownerID = ownerID;
        this.name = grocery_name;
        this.quantity = quantity;
        this.unit = unit;
        this.exp_date = exp_date;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("grocery_name", name);
        result.put("quantity", quantity);
        result.put("unit", unit);
        result.put("exp_date", exp_date);
        return result;
    }


}
