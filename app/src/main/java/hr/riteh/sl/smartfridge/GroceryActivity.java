package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import hr.riteh.sl.smartfridge.FirebaseDatabase.MyFridge;


public class GroceryActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, GroceryAdaper.OnGroceryListener {

    String userID;
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private Query fridges_query;
    private Query mess_query;
    private Query groceries_query;

    private List<String> fridge_list = new ArrayList<String>();
    private List<String> grocery_list_name = new ArrayList<String>();
    private List<String> grocery_list_quantity = new ArrayList<String>();
    private List<String> grocery_list_exp_date = new ArrayList<String>();
    private List<String> grocery_id_list = new ArrayList<String>();
    private List<String> fridge_id_list = new ArrayList<String>();
    int selected_fridge = 0;
    long countFridge = 0;
    private boolean updated_grocery;

    LinearLayout home_layout;

    ArrayAdapter adapter;
    GroceryAdaper groceryAdaper;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.grocery);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.grocery:

                        return true;
                    case R.id.fridge:
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.recipes:
                        startActivity(new Intent(getApplicationContext(), RecipesActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.stores:
                        startActivity(new Intent(getApplicationContext(), StoresActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                }
                return false;
            }
        });
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); //bellow setSupportActionBar(toolbar);
        getSupportActionBar().setCustomView(R.layout.titlebar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            updated_grocery = extras.getBoolean("selected_grocery");
            if (updated_grocery) {
                Toast.makeText(GroceryActivity.this, "Grocery updated", Toast.LENGTH_LONG).show();
            }
        }


        Spinner spinner = (Spinner) findViewById(R.id.grocery_fridge_spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, fridge_list);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        getAllFridges();
        recyclerView = findViewById(R.id.recycler_view);
        groceryAdaper = new GroceryAdaper(this, grocery_list_name, grocery_list_quantity, grocery_list_quantity, grocery_list_exp_date, this);
        recyclerView.setAdapter(groceryAdaper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.grocery_btn_newGrocery);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewGrocery();
            }
        });
    }

    //change fridge - spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // On selecting a spinner item
        String fridge_name = parent.getItemAtPosition(pos).toString();
        selected_fridge = (int) id;
        groceries_query = db.child("grocery").orderByChild("fridgeID").equalTo(fridge_id_list.get(selected_fridge));
        groceries_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    grocery_list_exp_date.clear();
                    grocery_list_name.clear();
                    grocery_list_quantity.clear();
                    grocery_id_list.clear();
                    // dataSnapshot is the "grocery" node with all children with id userID
                    for (DataSnapshot groceries : snapshot.getChildren()) {
                        Grocery groceryData = groceries.getValue(Grocery.class);
                        grocery_list_name.add(groceryData.grocery_name);
                        grocery_list_quantity.add(String.valueOf(groceryData.quantity));
                        grocery_list_exp_date.add(groceryData.exp_date);
                        grocery_id_list.add(groceries.getKey());
                    }
                    Collections.reverse(grocery_list_name);
                    Collections.reverse(grocery_list_quantity);
                    Collections.reverse(grocery_list_exp_date);
                    Collections.reverse(grocery_id_list);
                    System.out.println("tu sam");
                    groceryAdaper.notifyDataSetChanged();

                } else {
                    Toast.makeText(GroceryActivity.this, "You dont have any groceries", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroceryActivity.this, "Something wrong happened with groceries", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    private void getAllFridges() {
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fridges_query = db.child("fridges").orderByChild("ownerID").equalTo(userID);

        fridges_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fridge_list.clear();
                    // dataSnapshot is the "fridges" node with all children with id userID
                    for (DataSnapshot fridges : snapshot.getChildren()) {
                        MyFridge fridgeData = fridges.getValue(MyFridge.class);
                        if (!fridgeData.primary) {
                            fridge_list.add(fridgeData.name);
                            fridge_id_list.add(fridges.getKey());
                        } else {
                            fridge_list.add(0, fridgeData.name);
                            fridge_id_list.add(0, fridges.getKey());
                        }
                    }
                    adapter.notifyDataSetChanged();
                    countFridge = snapshot.getChildrenCount();
                } else {
                    Toast.makeText(GroceryActivity.this, "You dont have any fridges", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroceryActivity.this, "Something wrong happened with fridge", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }


    private void createNewGrocery() {

      /*  AlertDialog.Builder builder = new AlertDialog.Builder(this);


        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_create_grocery, null);
        builder.setView(customLayout);
        TextView fridge_name = customLayout.findViewById(R.id.fridge_txt);
        fridge_name.setText("Fridge: " + fridge_list.get(selected_fridge));
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
                String fridgeId = fridge_id_list.get(selected_fridge);
                Grocery msg = new Grocery(ownerID, fridgeId, grocery_name, quantity, exp_date);

                if (!grocery_name.matches("") && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    //username_textview.setText(msg.text);
                    FirebaseDatabase.getInstance().getReference().child("grocery").push().setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(GroceryActivity.this, "Grocery saved!", Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(GroceryActivity.this, "Failed to create grocery! Try again.", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                } else {
                    Toast.makeText(GroceryActivity.this, "You must enter text", Toast.LENGTH_LONG).show();
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
*/
    }

    @Override
    public void onGroceryClick(int position) {
        Intent intent = new Intent(this, GroceryItemActivity.class);
        String grocery_id = grocery_id_list.get(position);
        intent.putExtra("selected_grocery", grocery_id);
        startActivity(intent);
    }
}