package hr.riteh.sl.smartfridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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

import hr.riteh.sl.smartfridge.FirebaseDatabase.Grocery;

public class GroceryItemActivity extends AppCompatActivity {

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private Query grocery_query;
    private String grocery_name;
    private String grocery_quantity;
    private String grocery_exp_date;

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
        String grocery_id = extras.getString("selected_grocery");
        System.out.println(grocery_id);
        /*grocery_query = db.child("grocery").orderByChild("groceryID").equalTo(grocery_id);
        grocery_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Grocery groceryData = snapshot.getValue(Grocery.class);
                grocery_name = groceryData.grocery_name;
                grocery_quantity = groceryData.quantity;
                grocery_exp_date = groceryData.exp_date;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroceryItemActivity.this, "Something wrong happened with groceries", Toast.LENGTH_LONG).show();
            }
        });*/

        /*db.child("grocery").child(groceryID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                }
            }
        });
*/

        System.out.println(grocery_name);


    }
}
