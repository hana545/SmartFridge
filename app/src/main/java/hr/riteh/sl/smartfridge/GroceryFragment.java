package hr.riteh.sl.smartfridge;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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


public class GroceryFragment extends Fragment implements GroceryAdaper.OnGroceryListener {

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private Query groceries_query;

    GroceryAdaper groceryAdaper;
    RecyclerView recyclerView;

    private List<String> grocery_list_name = new ArrayList<String>();
    private List<String> grocery_list_quantity = new ArrayList<String>();
    private List<String> grocery_list_exp_date = new ArrayList<String>();
    private List<String> grocery_id_list = new ArrayList<String>();

    private String fridgeID;
    private String fridge_name;

    private boolean updated_grocery;

    public GroceryFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grocery, container, false);


        //////////
        Log.i("MESSAGEGETFRIDGE", "onCreateView: USAO ");
        recyclerView = view.findViewById(R.id.recycler_view_grocery);
        groceryAdaper = new GroceryAdaper(getActivity(), grocery_list_name, grocery_list_quantity, grocery_list_exp_date, this);
        recyclerView.setAdapter(groceryAdaper);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));


        if (getArguments() != null){
            fridgeID = getArguments().getString("fridgeID");
            fridge_name = getArguments().getString("fridge_name");
           // Log.i("MESSAGEGETFRIDGE", "onCreateView: uzme argument id="+fridgeID+" name: "+fridge_name);
            showGroceries();
        } else {
            fridgeID = "null";
            fridge_name = "null";
           // Log.i("MESSAGEGETFRIDGE", "onCreateView: ne uzme argument");
        }

       // Log.i("MESSAGEGETFRIDGE", "onCreateView: arg="+fridgeID);


////////////////


        FloatingActionButton fab = view.findViewById(R.id.grocery_btn_newGrocery);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Log.i("MESSAGEGETFRIDGE", "OnClickNewGrocery: usao");
                createNewGrocery();
            }
        });

        return view;
    }

    private void showGroceries(){

        groceries_query = db.child("grocery").orderByChild("fridgeID").equalTo(fridgeID);
        groceries_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                grocery_list_exp_date.clear();
                grocery_list_name.clear();
                grocery_list_quantity.clear();
                grocery_id_list.clear();
                if (snapshot.exists()) {
                    // dataSnapshot is the "grocery" node with all children with id userID
                    for (DataSnapshot groceries : snapshot.getChildren()) {
                        Grocery groceryData = groceries.getValue(Grocery.class);
                        grocery_list_name.add(groceryData.grocery_name);
                        grocery_list_quantity.add(groceryData.quantity);
                        grocery_list_exp_date.add(groceryData.exp_date);
                        grocery_id_list.add(groceries.getKey());
                    }
                    Collections.reverse(grocery_list_name);
                    Collections.reverse(grocery_list_quantity);
                    Collections.reverse(grocery_list_exp_date);
                    Collections.reverse(grocery_id_list);
                    //System.out.println("tu sam");

                } else {
                    Toast.makeText(MyApplication.getAppContext(), "You dont have any groceries in "+fridge_name, Toast.LENGTH_LONG).show();
                }
                groceryAdaper.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyApplication.getAppContext(), "Something wrong happened with groceries", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createNewGrocery() {

        //Log.i("MESSAGEGETFRIDGE", "createNewGrocery: pzvano");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_create_grocery, null);
        builder.setView(customLayout);
        TextView fridge_title = customLayout.findViewById(R.id.fridge_txt);
        fridge_title.setText("Fridge: " + fridge_name);
        EditText edt_grocery_name = (EditText) customLayout.findViewById(R.id.dialog_grocery_grocery_name);
        EditText edt_quantity = (EditText) customLayout.findViewById(R.id.dialog_grocery_quantity);
        EditText edt_exp_date = (EditText) customLayout.findViewById(R.id.dialog_grocery_exp_date);

        // add create and cancel buttons
        builder.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String grocery_name = edt_grocery_name.getText().toString();
                String quantity = edt_quantity.getText().toString();
                String exp_date = edt_exp_date.getText().toString();
                String ownerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String fridgeId = fridgeID;
                Grocery msg = new Grocery(ownerID, fridgeId, grocery_name, quantity, exp_date);

                if (!grocery_name.matches("") && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    //username_textview.setText(msg.text);
                    FirebaseDatabase.getInstance().getReference().child("grocery").push().setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showGroceries();
                                Toast.makeText(MyApplication.getAppContext(), "Grocery saved!", Toast.LENGTH_LONG).show();

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
        // Create new fragment and transaction
        Fragment mGroceryItemFragment = new GroceryItemFragment();

        //set parametar
        Bundle args = new Bundle();
        args.putString("selected_grocery", grocery_id_list.get(position));
        args.putString("fridgeID", fridgeID);
        args.putString("fridge_name", fridge_name);
        mGroceryItemFragment.setArguments(args);
        FragmentManager ft = getFragmentManager();
        ft.beginTransaction().replace(R.id.fragment_container, mGroceryItemFragment).addToBackStack(null).commit();


       /* Intent intent = new Intent(MyApplication.getAppContext(), GroceryItemActivity.class);
        String grocery_id = grocery_id_list.get(position);
        intent.putExtra("selected_grocery", grocery_id);
        startActivity(intent);*/
    }

}