package hr.riteh.sl.smartfridge;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Grocery;

public class GroceryItemFragment extends Fragment {


    private View view;

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private Query grocery_query;
    private String grocery_name;
    private String grocery_quantity;
    private String grocery_exp_date;

    private String grocery_id = "";
    private String fridgeID;
    private String fridge_name;

    EditText editName;
    EditText editQuantity;
    EditText edit_exp_date;

    public GroceryItemFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_grocery_item, container, false);

        if (getArguments() != null){
            grocery_id = getArguments().getString("selected_grocery");
            fridgeID = getArguments().getString("fridgeID");
            fridge_name = getArguments().getString("fridge_name");
        } else {
            grocery_id = "";
            fridgeID = "";
            fridge_name = "";
        }

        fillGroceryData();

        Fragment mGroceryFragment = new GroceryFragment();
        mGroceryFragment.setArguments(getArguments());
        FragmentManager ft = getFragmentManager();

        Button save = view.findViewById(R.id.grocery_item_save);
        Button delete = view.findViewById(R.id.grocery_item_delete);

        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                editGrocery();
                ft.beginTransaction().replace(R.id.fragment_container, mGroceryFragment).addToBackStack(null).commit();
                Toast.makeText(MyApplication.getAppContext(), "Grocery updated", Toast.LENGTH_LONG).show();
            }
        });


        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                deleteGrocery();
                ft.beginTransaction().replace(R.id.fragment_container, mGroceryFragment).addToBackStack(null).commit();

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void fillGroceryData(){
        editName = view.findViewById(R.id.grocery_item_name);
        editQuantity = view.findViewById(R.id.grocery_item_quantity);
        edit_exp_date = view.findViewById(R.id.grocery_item_exp_date);
        //System.out.println(grocery_id);
        grocery_query = db.child("grocery").child(grocery_id);
        grocery_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Grocery groceryData = snapshot.getValue(Grocery.class);
                if(groceryData != null) {
                    grocery_name = groceryData.grocery_name;
                    grocery_quantity = groceryData.quantity;
                    grocery_exp_date = groceryData.exp_date;

                    editName.setText(grocery_name);
                    editQuantity.setText(grocery_quantity);
                    edit_exp_date.setText(grocery_exp_date);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyApplication.getAppContext(), "Something wrong happened with groceries", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void editGrocery() {
        editName = view.findViewById(R.id.grocery_item_name);
        editQuantity = view.findViewById(R.id.grocery_item_quantity);
        edit_exp_date = view.findViewById(R.id.grocery_item_exp_date);
        grocery_query = db.child("grocery").child(grocery_id);
        grocery_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> groceryValues = new HashMap<String,Object>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    groceryValues.put(snap.getKey(), snap.getValue());
                }
                groceryValues.put("grocery_name", editName.getText().toString());
                groceryValues.put("quantity", editQuantity.getText().toString());
                groceryValues.put("exp_date", edit_exp_date.getText().toString());
                db.child("grocery").child(grocery_id).updateChildren(groceryValues);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
    }

    public void deleteGrocery(){
        grocery_query = db.child("grocery").child(grocery_id);
        grocery_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot grocery: snapshot.getChildren()) {
                    grocery.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });

    }
}