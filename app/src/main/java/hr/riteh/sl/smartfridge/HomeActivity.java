package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import hr.riteh.sl.smartfridge.FirebaseDatabase.User;

public class HomeActivity extends AppCompatActivity {

    private TextView Username;

    private String userID;
    private FirebaseUser user;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.fridge);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.grocery:
                        startActivity(new Intent(getApplicationContext(), GroceryActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.fridge:
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



        Username = (TextView)findViewById(R.id.home_username);


        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        db = FirebaseDatabase.getInstance().getReference("users").child(userID);



        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userdata = snapshot.getValue(User.class);

                if(userdata != null){
                    Username.setText(userdata.name);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Something wrong happened", Toast.LENGTH_LONG).show();
            }
        });



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_myprofile:
                Toast.makeText(this, "Will go to myprofile", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.options_settings:
                Toast.makeText(this, "Will go to my settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.options_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}