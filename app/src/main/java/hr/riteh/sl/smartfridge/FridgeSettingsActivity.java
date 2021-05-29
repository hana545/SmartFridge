package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Fridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.Message;
import hr.riteh.sl.smartfridge.FirebaseDatabase.User;

public class FridgeSettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ArrayAdapter adapter;
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    private List<String> fridge_list = new ArrayList<String>();
    private List<String> fridge_id_list = new ArrayList<String>();

    private List<String> fridge_new_names = new ArrayList<String>();

    private String primary_old;
    private String primary_new;
    int selected_fridge = 0;
    int countFridge = 0;

    EditText fridge1;
    EditText fridge2;
    EditText fridge3;
    Button btn_del_fridge1;
    Button btn_del_fridge2;
    Button btn_del_fridge3;
    Button btn_people_fridge1;
    Button btn_people_fridge2;
    Button btn_people_fridge3;
    Button btn_add_people_fridge1;
    Button btn_add_people_fridge2;
    Button btn_add_people_fridge3;
    Button btn_new_fridge;

    Boolean exists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fridge_settings);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); //bellow setSupportActionBar(toolbar);
        getSupportActionBar().setCustomView(R.layout.titlebar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME); //bellow setSupportActionBar(toolbar);
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        getAllFridges();
        //change fridge
        Spinner spinner = (Spinner) findViewById(R.id.settings_fridge_spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, fridge_list);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        fridge1 = (EditText) findViewById(R.id.settings_fridge_name1);
        fridge2 = (EditText) findViewById(R.id.settings_fridge_name2);
        fridge3 = (EditText) findViewById(R.id.settings_fridge_name3);


        btn_del_fridge1 = (Button) findViewById(R.id.settings_delete_fridge1);
        btn_del_fridge2 = (Button) findViewById(R.id.settings_delete_fridge2);
        btn_del_fridge3 = (Button) findViewById(R.id.settings_delete_fridge3);

        btn_people_fridge1 = (Button) findViewById(R.id.settings_people_fridge1);
        btn_people_fridge2 = (Button) findViewById(R.id.settings_people_fridge2);
        btn_people_fridge3 = (Button) findViewById(R.id.settings_people_fridge3);

        btn_add_people_fridge1 = (Button) findViewById(R.id.settings_add_people_fridge1);
        btn_add_people_fridge2 = (Button) findViewById(R.id.settings_add_people_fridge2);
        btn_add_people_fridge3 = (Button) findViewById(R.id.settings_add_people_fridge3);

        btn_new_fridge = (Button) findViewById(R.id.settings_add_new);

        Button bnt_save = (Button) findViewById(R.id.settings_save);

        bnt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!primary_new.equals(primary_old)   ) {
                  updatePrimaryFridges(fridge_id_list.get(selected_fridge));
                }
                fridge_new_names.add(fridge1.getText().toString());
                fridge_new_names.add(fridge2.getText().toString());
                fridge_new_names.add(fridge3.getText().toString());
                for (Integer i = 0; i < countFridge; i++) {
                    if (fridge_new_names.get(i).equals("")) {
                        Toast.makeText(FridgeSettingsActivity.this, "Name can not be empty or the same as old one", Toast.LENGTH_LONG).show();
                    } else if (fridge_new_names.get(i).length() > 15) {
                        Toast.makeText(FridgeSettingsActivity.this, "Name is to long", Toast.LENGTH_LONG).show();
                    } else if (!fridge_new_names.get(i).equals(fridge_list.get(i))) {
                        updateNameFridge(fridge_id_list.get(i), fridge_new_names.get(i));
                    } else {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                }
            }
        });

    }


    private void checkFridges() {
        Integer count = 0;
        if (btn_del_fridge1.isEnabled()) count++;
        if (btn_del_fridge2.isEnabled()) count++;
        if (btn_del_fridge3.isEnabled()) count++;
        if (count <= 1){
            btn_del_fridge1.setEnabled(false);
            btn_del_fridge2.setEnabled(false);
            btn_del_fridge3.setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String fridge_name = parent.getItemAtPosition(position).toString();
        selected_fridge = (int) id;
       //Log.i("SETTTINGSFRIDGE", "selektira neki u spinneru");
        if(!fridge_id_list.isEmpty()) primary_new = fridge_id_list.get(selected_fridge);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void getAllFridges() {
        ProgressDialog mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Loading ...");
        mProgressDialog.show();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query fridges_query = FirebaseDatabase.getInstance().getReference().child("fridges").orderByChild("ownerID").equalTo(userID);

       //Log.i("SETTTINGSFRIDGE", "ide uzet frigeve");
        fridges_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fridge_list.clear();
                    fridge_id_list.clear();
                    // dataSnapshot is the "fridges" node with all children with id userID
                    for (DataSnapshot fridges : snapshot.getChildren()) {
                        Fridge fridgeData = fridges.getValue(Fridge.class);
                        if (!fridgeData.primary) {
                            fridge_list.add(fridgeData.name);
                            fridge_id_list.add(fridges.getKey());
                        } else {
                            primary_old = fridges.getKey();
                            fridge_list.add(0, fridgeData.name);
                            fridge_id_list.add(0, fridges.getKey());
                        }
                    }
                    countFridge = fridge_list.size();
                    fillData();
                    adapter.notifyDataSetChanged();
                    mProgressDialog.dismiss();
                } else {
                    Toast.makeText(FridgeSettingsActivity.this, "You dont have any fridges", Toast.LENGTH_LONG).show();
                    mProgressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FridgeSettingsActivity.this, "Something wrong happened with fridge", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void fillData() {
        fridge1.setText("");
        fridge2.setText("");
        fridge3.setText("");

        btn_new_fridge.setVisibility(View.VISIBLE);
        btn_new_fridge.setEnabled(true);

        if(countFridge > 0){
            fridge1.setVisibility(View.VISIBLE);
            findViewById(R.id.settings_name1).setVisibility(View.VISIBLE);
            btn_del_fridge1.setEnabled(true);
            btn_del_fridge1.setVisibility(View.VISIBLE);
            btn_add_people_fridge1.setEnabled(true);
            btn_add_people_fridge1.setVisibility(View.VISIBLE);
            btn_people_fridge1.setEnabled(true);
            btn_people_fridge1.setVisibility(View.VISIBLE);
            fridge1.setText(fridge_list.get(0));
        }
        if(countFridge > 1){
            fridge2.setVisibility(View.VISIBLE);
            findViewById(R.id.settings_name2).setVisibility(View.VISIBLE);
            btn_del_fridge2.setEnabled(true);
            btn_del_fridge2.setVisibility(View.VISIBLE);
            btn_add_people_fridge2.setEnabled(true);
            btn_add_people_fridge2.setVisibility(View.VISIBLE);
            btn_people_fridge2.setEnabled(true);
            btn_people_fridge2.setVisibility(View.VISIBLE);
            fridge2.setText(fridge_list.get(1));
        }
        if(countFridge > 2){
            fridge3.setVisibility(View.VISIBLE);
            findViewById(R.id.settings_name3).setVisibility(View.VISIBLE);
            btn_del_fridge3.setEnabled(true);
            btn_del_fridge3.setVisibility(View.VISIBLE);
            btn_add_people_fridge3.setEnabled(true);
            btn_add_people_fridge3.setVisibility(View.VISIBLE);
            btn_people_fridge3.setEnabled(true);
            btn_people_fridge3.setVisibility(View.VISIBLE);
            fridge3.setText(fridge_list.get(2));

            btn_new_fridge.setVisibility(View.INVISIBLE);
            btn_new_fridge.setEnabled(false);
        }
        checkFridges();

        //check delete buttons
        if(btn_del_fridge1.isEnabled()) {
            btn_del_fridge1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_delete_fridge(fridge_id_list.get(0), 0);
                }
            });
        }
        if(btn_del_fridge2.isEnabled()) {
            btn_del_fridge2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_delete_fridge(fridge_id_list.get(1), 1);
                }
            });
        }

        if(btn_del_fridge3.isEnabled()) {
            btn_del_fridge3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_delete_fridge(fridge_id_list.get(2), 2);
                }
            });
        }

        if(btn_add_people_fridge1.isEnabled()) {
            btn_add_people_fridge1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewMember(0);
                }
            });
        }
        if(btn_add_people_fridge2.isEnabled()) {
            btn_add_people_fridge2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewMember(1);
                }
            });
        }

        if(btn_add_people_fridge3.isEnabled()) {
            btn_add_people_fridge3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewMember(2);
                }
            });
        }

        if(btn_new_fridge.isEnabled()) {
            btn_new_fridge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createNewFridge();
                }
            });
        }

    }
    private void createNewFridge() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_create_fridge, null);
        builder.setView(customLayout);

        TextView countF = (TextView) customLayout.findViewById(R.id.dialog_countFridge);
        EditText fridgeName = (EditText) customLayout.findViewById(R.id.dialog_fridge_name);
        CheckBox primaryFridge = (CheckBox) customLayout.findViewById(R.id.dialog_defaultFridge);
        countF.setText((countFridge+1)+". fridge out of 3");

        // add create and cancel buttons
        builder.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String FName = fridgeName.getText().toString();
                Boolean primary = primaryFridge.isChecked();
                String ownerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Fridge newFridge = new Fridge(FName, ownerID, primary);
                if (!FName.matches("") && FirebaseAuth.getInstance().getCurrentUser() != null && FName.length() < 15) {
                    db.child("fridges").orderByChild("name").equalTo(FName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(FridgeSettingsActivity.this, "You already have a fridge with this name", Toast.LENGTH_LONG).show();
                            } else {
                                String key = FirebaseDatabase.getInstance().getReference().child("fridges").push().getKey();
                                db.child("fridges").child(key).setValue(newFridge).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(FridgeSettingsActivity.this, "Fridge created!", Toast.LENGTH_LONG).show();
                                            if(primaryFridge.isChecked()) updatePrimaryFridges(key);
                                            Intent intent = getIntent();
                                            finish();
                                            startActivity(intent);
                                            // ((MessagesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getFridgeMessages(fridge_id_list.get(selected_fridge));
                                        } else {
                                            Toast.makeText(FridgeSettingsActivity.this, "Failed to create fridge! Try again.", Toast.LENGTH_LONG).show();

                                        }
                                    }
                                });

                                dialog.dismiss();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                            Toast.makeText(FridgeSettingsActivity.this, "Canceled", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });

                } else if (FName.length() > 15) {
                    Toast.makeText(FridgeSettingsActivity.this, "Name is to long", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FridgeSettingsActivity.this, "You must give a name", Toast.LENGTH_LONG).show();
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

    private void addNewMember(Integer selected_fridge) {

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);

        View view  = getLayoutInflater().inflate(R.layout.dialog_add_new_member, null);
        dialog.setContentView(view);

        EditText memberEmail = (EditText) view.findViewById(R.id.dialog_input_email);
        Button btn_add = (Button) view.findViewById(R.id.dialog_add_member);

        exists = false;

        btn_add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String newEmail = memberEmail.getText().toString();
                Fridge currentFridge = new Fridge(fridge_list.get(selected_fridge), userID, false);

                if (!newEmail.equals("") && newEmail.contains("@") && !newEmail.equals(userEmail)) {
                    db.child("users").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String memberID = "";
                                for (DataSnapshot users : snapshot.getChildren()) {
                                    User userData = users.getValue(User.class);
                                    if (userData != null) {
                                        String email = userData.email;
                                        if (userData.email.equals(newEmail)) {
                                            exists = true;
                                            memberID = users.getKey();
                                        }
                                    }
                                }
                                if (exists) {
                                    assert memberID != null;
                                    /*FirebaseDatabase.getInstance().getReference().setValue(currentFridge).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(FridgeSettingsActivity.this, "Member added!", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(FridgeSettingsActivity.this, "Failed to create fridge! Try again.", Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });*/
                                    Toast.makeText(FridgeSettingsActivity.this, "will add member!", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(FridgeSettingsActivity.this, "There is no user with that email", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(MyApplication.getAppContext(), "There are no other users", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MyApplication.getAppContext(), "Something wrong happened while checking for the user", Toast.LENGTH_LONG).show();
                        }
                    });

                } else if (newEmail.equals(userEmail)){
                    Toast.makeText(FridgeSettingsActivity.this, "That is your email you stupid", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(FridgeSettingsActivity.this, "Invalid email", Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog.show();


    }

    public void updatePrimaryFridges(String primary_fridge) {

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for(Integer i = 0; i < countFridge; i++) {
            HashMap<String, Object> changePrimary = new HashMap<>();
            if (fridge_id_list.get(i).equals(primary_fridge)){
                changePrimary.put("primary", true);
                FirebaseDatabase.getInstance().getReference().child("fridges").child(fridge_id_list.get(i)).updateChildren(changePrimary);
            } else {
                changePrimary.put("primary", false);
                FirebaseDatabase.getInstance().getReference().child("fridges").child(fridge_id_list.get(i)).updateChildren(changePrimary);
            }

        }
    }

    private void updateNameFridge(String fridgeID, String newName) {

        HashMap<String, Object> changeName = new HashMap<>();
        changeName.put("name", newName);
        FirebaseDatabase.getInstance().getReference().child("fridges").child(fridgeID).updateChildren(changeName);
    }


    private void dialog_delete_fridge(String fridgeID, Integer fridge_order) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to delete "+ fridge_list.get(fridge_order)+" fridge").setTitle("Delete fridge");

        // add create and cancel buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete_messages_and_groceries(fridgeID);
                DatabaseReference mPostReference = FirebaseDatabase.getInstance().getReference()
                        .child("fridges").child(fridgeID);
                mPostReference.removeValue();
                
                Toast.makeText(FridgeSettingsActivity.this, "Deleted "+fridge_list.get(fridge_order)+" fridge", Toast.LENGTH_LONG).show();
                Intent intent = getIntent();
                finish();
                startActivity(intent);

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void delete_messages_and_groceries(String fridgeID) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        ///delete messages
        db.child("messages").orderByChild("fridgeID").equalTo(fridgeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot != null){
                    for (DataSnapshot message: snapshot.getChildren()) {
                        message.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG: ", databaseError.getMessage());
            }
        });

        ///delete grocery
        db.child("grocery").orderByChild("fridgeID").equalTo(fridgeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot grocery: snapshot.getChildren()) {
                    if(snapshot.exists() && snapshot != null) {
                        grocery.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });


    }

}