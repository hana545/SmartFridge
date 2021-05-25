package hr.riteh.sl.smartfridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Grocery;

public class GroceryItemActivity extends AppCompatActivity {

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private Query grocery_query;
    private String grocery_name;
    private String grocery_quantity;
    private String grocery_exp_date;
    private String grocery_id = "";

    EditText editName;
    EditText editQuantity;
    EditText edit_exp_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_item);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.grocery);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.grocery:
                        startActivity(new Intent(getApplicationContext(), GroceryActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.fridge:
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.recipes:
                        startActivity(new Intent(getApplicationContext(), RecipesActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.stores:
                        startActivity(new Intent(getApplicationContext(), StoresActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                }
                return false;
            }
        });
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); //bellow setSupportActionBar(toolbar);
        getSupportActionBar().setCustomView(R.layout.titlebar);

        Bundle extras = getIntent().getExtras();
        grocery_id = extras.getString("selected_grocery");
        editName = findViewById(R.id.grocery_item_name);
        editQuantity = findViewById(R.id.grocery_item_quantity);
        edit_exp_date = findViewById(R.id.grocery_item_exp_date);
        //System.out.println(grocery_id);
        grocery_query = db.child("grocery").child(grocery_id);
        grocery_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Grocery groceryData = snapshot.getValue(Grocery.class);
                grocery_name = groceryData.grocery_name;
                grocery_quantity = groceryData.quantity;
                grocery_exp_date = groceryData.exp_date;

                editName.setText(grocery_name);
                editQuantity.setText(grocery_quantity);
                edit_exp_date.setText(grocery_exp_date);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroceryItemActivity.this, "Something wrong happened with groceries", Toast.LENGTH_LONG).show();
            }
        });

        Button save = findViewById(R.id.grocery_item_save);
        Button back = findViewById(R.id.grocery_item_back);
        Button delete = findViewById(R.id.grocery_item_delete);

        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                editGrocery();
                Intent intent = new Intent(GroceryItemActivity.this, GroceryActivity.class);
                intent.putExtra("grocery_updated", true);
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroceryItemActivity.this, GroceryActivity.class);
                intent.putExtra("grocery_updated", false);
                startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                deleteGrocery();
                Intent intent = new Intent(GroceryItemActivity.this, GroceryActivity.class);
                intent.putExtra("grocery_updated", false);
                startActivity(intent);
            }
        });

    }

    public void editGrocery() {
        editName = findViewById(R.id.grocery_item_name);
        editQuantity = findViewById(R.id.grocery_item_quantity);
        edit_exp_date = findViewById(R.id.grocery_item_exp_date);
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
