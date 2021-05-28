package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.Spinner;
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

public class FridgeSettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ArrayAdapter adapter;


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
    String fridge1_name = "";
    String fridge2_name = "";
    String fridge3_name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fridge_settings);


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

       //Log.i("SETTTINGSFRIDGE", "dodao spiner, inicijalizirao buttone i edit text");


        btn_del_fridge1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_del_fridge1.isEnabled()) {
                   dialog_delete_fridge(fridge_id_list.get(0), 0);
                } else {
                    Toast.makeText(FridgeSettingsActivity.this, "Can not delete this", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_del_fridge2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_del_fridge2.isEnabled()) {
                    dialog_delete_fridge(fridge_id_list.get(1), 1);
                } else {
                    Toast.makeText(FridgeSettingsActivity.this, "Can not delete this", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_del_fridge3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_del_fridge3.isEnabled()) {
                    dialog_delete_fridge(fridge_id_list.get(2), 2);
                } else {
                    Toast.makeText(FridgeSettingsActivity.this, "Can not delete this", Toast.LENGTH_LONG).show();
                }
            }
        });


        Button bnt_save = (Button) findViewById(R.id.settings_save);
       //Log.i("SETTTINGSFRIDGE", "dodao button save");

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
                    } else if (!fridge_new_names.get(i).equals(fridge_list.get(i))) {
                        updateNameFridge(fridge_id_list.get(i), fridge_new_names.get(i));
                    }
                }
                Intent intent = new Intent(FridgeSettingsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
       //Log.i("SETTTINGSFRIDGE", "kraj");

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
        if(countFridge > 0){
           //Log.i("SETTTINGSFRIDGE", "Fokusira 1. + enable delete "+fridge_list.get(0));
            fridge1.setFocusable(true);
            fridge1.setFocusableInTouchMode(true);
            btn_del_fridge1.setEnabled(true);
            fridge1.setText(fridge_list.get(0));
        }
        if(countFridge > 1){
           //Log.i("SETTTINGSFRIDGE", "Fokusira 2. + enable delete "+fridge_list.get(1));
            fridge2.setFocusable(true);
            fridge2.setFocusableInTouchMode(true);
            btn_del_fridge2.setEnabled(true);
            fridge2.setText(fridge_list.get(1));
        }
        if(countFridge > 2){
           //Log.i("SETTTINGSFRIDGE", "Fokusira 3. + eneble delete  "+fridge_list.get(2));
            fridge3.setFocusable(true);
            fridge3.setFocusableInTouchMode(true);
            btn_del_fridge3.setEnabled(true);
            fridge3.setText(fridge_list.get(2));
        }
        checkFridges();

       //Log.i("SETTTINGSFRIDGE", "puni gotov");
    }

    public void updatePrimaryFridges(String primary_fridge) {

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Query fridges_query = FirebaseDatabase.getInstance().getReference("fridges").orderByChild("ownerID").equalTo(userID);

        fridges_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // dataSnapshot is the "fridges" node with all children with id userID

                    for (DataSnapshot fridges : snapshot.getChildren()) {
                        Fridge fridgeData = fridges.getValue(Fridge.class);
                        if (!primary_new.equals(primary_old)) {
                            HashMap<String, Object> changePrimary = new HashMap<>();
                            if (fridgeData.primary && !fridges.getKey().equals(primary_fridge)) {
                                changePrimary.put("primary", false);
                                FirebaseDatabase.getInstance().getReference().child("fridges").child(fridges.getKey()).updateChildren(changePrimary);
                               //Log.i("FIREBASE", "CHANGED PRIMARY-" + fridgeData.name);
                            } else if (fridges.getKey().equals(primary_fridge)) {
                                changePrimary.put("primary", true);
                                FirebaseDatabase.getInstance().getReference().child("fridges").child(fridges.getKey()).updateChildren(changePrimary);
                            }
                        }
                    }

                    Toast.makeText(FridgeSettingsActivity.this, "Saved!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FridgeSettingsActivity.this, "You dont have any fridges", Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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