package hr.riteh.sl.smartfridge;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Grocery;
import hr.riteh.sl.smartfridge.FirebaseDatabase.GrocerySH;

public class ShoppingListFragment extends Fragment implements ShoppingListAdapter.OnGroceryListener {

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private Query groceries_query;


    ShoppingListAdapter groceryAdaper;
    RecyclerView recyclerView;
    ArrayAdapter unitAdapter;

    private List<String> grocery_list_name = new ArrayList<String>();
    private List<String> grocery_list_quantity = new ArrayList<String>();
    private List<String> grocery_list_unit = new ArrayList<String>();
    private List<String> grocery_list_exp_date = new ArrayList<String>();
    private List<String> grocery_id_list = new ArrayList<String>();

    private List<String> unit_list = new ArrayList<String>();
    private String selected_unit = "";

    private String fridgeID;
    private String fridge_name;
    private String ownerId;

    private View view;


    public ShoppingListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            fridgeID = getArguments().getString("fridgeID");
            fridge_name = getArguments().getString("fridge_name");

            showGroceriesSH();
        } else {
            fridgeID = "";
            fridge_name = "";
        }
        Spinner fridge_spinner = (Spinner) getActivity().findViewById(R.id.fridge_spinner);
        fridge_spinner.setEnabled(false);

        unit_list.add("kg");
        unit_list.add("g");
        unit_list.add("l");
        unit_list.add("ml");
        unit_list.add("pieces");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        // Inflate the layout for this fragment


        recyclerView = view.findViewById(R.id.recycler_view_grocery);
        groceryAdaper = new ShoppingListAdapter(getActivity(), fridgeID, grocery_id_list, grocery_list_name, grocery_list_quantity, grocery_list_unit, this);
        recyclerView.setAdapter(groceryAdaper);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        FloatingActionButton add = (FloatingActionButton) view.findViewById(R.id.grocery_sh_btn_newGrocery);
        add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // ((MessagesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getFridgeMessages(fridge_id_list.get(selected_fridge));
                createNewGrocerySH();
            }
        });

        return view;
    }

    private void showGroceriesSH(){

        groceries_query = db.child("shopping_lists").child(fridgeID);
        groceries_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                grocery_list_name.clear();
                grocery_list_quantity.clear();
                grocery_list_unit.clear();
                grocery_id_list.clear();
                if (snapshot.exists()) {
                    // dataSnapshot is the "grocery" node with all children with id userID
                    for (DataSnapshot groceries : snapshot.getChildren()) {
                        Grocery groceryData = groceries.getValue(Grocery.class);
                        grocery_list_name.add(groceryData.grocery_name);
                        grocery_list_quantity.add(String.valueOf(groceryData.quantity));
                        grocery_list_unit.add(groceryData.unit);
                        grocery_id_list.add(groceries.getKey());
                    }
                    Collections.reverse(grocery_list_name);
                    Collections.reverse(grocery_list_quantity);
                    Collections.reverse(grocery_list_unit);
                    Collections.reverse(grocery_id_list);
                    //System.out.println("tu sam");
                    view.findViewById(R.id.text_no_groceries).setVisibility(View.INVISIBLE);

                } else {
                    view.findViewById(R.id.text_no_groceries).setVisibility(View.VISIBLE);
                }
                groceryAdaper.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyApplication.getAppContext(), "Something wrong happened with groceries", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createNewGrocerySH() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_create_shopping_list_grocery, null);
        builder.setView(customLayout);
        TextView fridge_title = customLayout.findViewById(R.id.fridge_txt);
        fridge_title.setText(" Add to shopping list for fridge: " + fridge_name);

        Spinner unitSpinner = (Spinner) customLayout.findViewById(R.id.unit_spinner);
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                    selected_unit = item.toString();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO Auto-generated method stub

            }
        });
        unitAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, unit_list);
        // Specify the layout to use when the list of choices appears
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        unitSpinner.setAdapter(unitAdapter);

        EditText edt_grocery_name = (EditText) customLayout.findViewById(R.id.dialog_grocery_grocery_name);
        EditText edt_quantity = (EditText) customLayout.findViewById(R.id.dialog_grocery_quantity);
        // add create and cancel buttons
        builder.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String grocery_name = edt_grocery_name.getText().toString();
                Integer quantity = Integer.parseInt(edt_quantity.getText().toString());
                String ownerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String fridgeId = fridgeID;
                GrocerySH msg = new GrocerySH(ownerID, grocery_name, quantity, selected_unit);

                if (!grocery_name.matches("") && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    //username_textview.setText(msg.text);
                    FirebaseDatabase.getInstance().getReference().child("shopping_lists").child(fridgeID).push().setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showGroceriesSH();
                                Toast.makeText(MyApplication.getAppContext(), "Grocery saved to shoping list!", Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(MyApplication.getAppContext(), "Failed to create grocery! Try again.", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                } else {
                    Toast.makeText(MyApplication.getAppContext(), "You must enter text", Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onGroceryClick(int position) {

    }
}