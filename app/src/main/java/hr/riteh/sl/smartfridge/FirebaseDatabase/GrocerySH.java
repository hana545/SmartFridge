package hr.riteh.sl.smartfridge.FirebaseDatabase;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class GrocerySH {
    public String ownerID;
    public String grocery_name;
    public Integer quantity;
    public String unit;


    public GrocerySH(String ownerID, String grocery_name, Integer quantity, String unit) {
        this.ownerID = ownerID;
        this.grocery_name = grocery_name;
        this.quantity = quantity;
        this.unit = unit;
    }
    }
