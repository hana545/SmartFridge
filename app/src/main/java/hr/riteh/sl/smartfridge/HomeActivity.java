package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Fridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.Message;
import hr.riteh.sl.smartfridge.FirebaseDatabase.User;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String userID;
    private DatabaseReference db;
    private Query fridges_query;
    private Query mess_query;

    private List<String> fridge_list = new ArrayList<String>();
    private List<String> fridge_id_list = new ArrayList<String>();
    int selected_fridge = 0;
    long countFridge = 0;

    LinearLayout home_layout;

    ArrayAdapter adapter;

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
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.fridge:
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

        //change fridge
        Spinner spinner = (Spinner) findViewById(R.id.fridge_spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, fridge_list);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        getAllFridges();

        ///treba se popunit lista frizidera
        //String fridgeID = fridge_id_list.get(selected_fridge);
       // mess_query = FirebaseDatabase.getInstance().getReference().child("messages").orderByChild("fridgeID").equalTo(fridgeID);

        /*mess_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot mess : snapshot.getChildren()) {
                        Message messData = mess.getValue(Message.class);
                        Toast.makeText(HomeActivity.this, "yes", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(HomeActivity.this, "You dont have any messages", Toast.LENGTH_LONG).show();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Something wrong happened with fridge", Toast.LENGTH_LONG).show();
            }
        });*/


        FloatingActionButton fab = findViewById(R.id.home_btn_newMessage);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewMessage();
            }
        });
    }



    private void getAllFridges() {
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fridges_query = FirebaseDatabase.getInstance().getReference().child("fridges").orderByChild("ownerID").equalTo(userID);

        fridges_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fridge_list.clear();
                    // dataSnapshot is the "fridges" node with all children with id userID
                    for (DataSnapshot fridges : snapshot.getChildren()) {
                        Fridge fridgeData = fridges.getValue(Fridge.class);
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
                    Toast.makeText(HomeActivity.this, "You dont have any fridges", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Something wrong happened with fridge", Toast.LENGTH_LONG).show();
            }
        });
    }



    //create and show options menu
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
            case R.id.options_newfridge:
                Log.i("COUNT", "COUNT: " + countFridge);
                if (countFridge >= 3) {
                    Toast.makeText(this, "You already have 3 fridges", Toast.LENGTH_SHORT).show();
                } else {
                    createNewFridge();
                }
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

    //change fridge - spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // On selecting a spinner item
        String fridge_name = parent.getItemAtPosition(pos).toString();
        selected_fridge = (int) id;
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    private void createNewFridge() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_create_fridge, null);
        builder.setView(customLayout);
        EditText fridgeName = (EditText) customLayout.findViewById(R.id.dialog_fridge_name);
        CheckBox primaryFridge = (CheckBox) customLayout.findViewById(R.id.dialog_defaultFridge);

        // add create and cancel buttons
        builder.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String FName = fridgeName.getText().toString();
                Boolean primary = primaryFridge.isChecked();
                String ownerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Fridge fridge = new Fridge(FName, ownerID, primary);
                if (!FName.matches("") && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    FirebaseDatabase.getInstance().getReference().child("fridges").orderByChild("name").equalTo(FName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(HomeActivity.this, "You already have a fridge with this name", Toast.LENGTH_LONG).show();
                            } else {
                                String key = FirebaseDatabase.getInstance().getReference().child("fridges").push().getKey();
                                FirebaseDatabase.getInstance().getReference().child("fridges").child(key).setValue(fridge).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(HomeActivity.this, "Fridge created!", Toast.LENGTH_LONG).show();
                                            getAllFridges();
                                        } else {
                                            Toast.makeText(HomeActivity.this, "Failed to create fridge! Try again.", Toast.LENGTH_LONG).show();

                                        }
                                    }
                                });
                                if(primaryFridge.isChecked()) changePrimaryValue(key);
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                            Toast.makeText(HomeActivity.this, "Canceled", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });

                } else {
                    Toast.makeText(HomeActivity.this, "You must give a name", Toast.LENGTH_LONG).show();
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

    public void changePrimaryValue(String key) {

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fridges_query = FirebaseDatabase.getInstance().getReference("fridges").orderByChild("ownerID").equalTo(userID);

        fridges_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // dataSnapshot is the "fridges" node with all children with id userID

                    for (DataSnapshot fridges : snapshot.getChildren()) {
                        Fridge fridgeData = fridges.getValue(Fridge.class);
                        if (fridgeData.primary && !fridges.getKey().equals(key) ){
                            HashMap<String, Object> changePrimary = new HashMap<>();
                            changePrimary.put("primary", false);
                            FirebaseDatabase.getInstance().getReference().child("fridges").child(fridges.getKey()).updateChildren(changePrimary);
                            Log.i("FIREBASE", "CHANGED PRIMARY-" + fridgeData.name);
                        }
                        //Log.i("FIREBASE", "COUNT" + fridges.getChildrenCount());
                        //Log.i("FIREBASE", "NAME" + fridges.child("name"));


                    }
                } else {
                    Toast.makeText(HomeActivity.this, "You dont have any fridges", Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createNewMessage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_create_message, null);
        builder.setView(customLayout);
        EditText message_text = (EditText) customLayout.findViewById(R.id.dialog_message_text);

        // add create and cancel buttons
        builder.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = message_text.getText().toString();
                String ownerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String fridgeId = fridge_id_list.get(selected_fridge);
                Message msg = new Message(text, ownerID, fridgeId);

                if (!text.matches("") && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    //username_textview.setText(msg.text);
                    FirebaseDatabase.getInstance().getReference().child("messages").push().setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(HomeActivity.this, "Message posted!", Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(HomeActivity.this, "Failed to create message! Try again.", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                } else {
                    Toast.makeText(HomeActivity.this, "You must enter text", Toast.LENGTH_LONG).show();
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
}
