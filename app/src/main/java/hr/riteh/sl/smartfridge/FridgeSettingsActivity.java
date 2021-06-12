package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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

import hr.riteh.sl.smartfridge.FirebaseDatabase.Fridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.MyFridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.User;
import hr.riteh.sl.smartfridge.SendNotification.Notifications;

public class FridgeSettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, FridgeMembersAdapter.OnMemberListener {

    ArrayAdapter adapter;
    Spinner spinner;

    FirebaseUser Fuser = FirebaseAuth.getInstance().getCurrentUser();
    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    private List<String> fridge_list = new ArrayList<String>();
    private List<String> fridge_id_list = new ArrayList<String>();

    private List<String> my_fridge_list = new ArrayList<String>();
    private List<String> my_fridge_id_list = new ArrayList<String>();

    private List<String> fridge_new_names = new ArrayList<String>();
    String memberID;
    String memberName = "";

    private String primaryId_old;
    private String primaryId_new;
    int selected_fridge = 0;
    int countMyFridge = 0;
    int countAllFridge = 0;

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

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME); //bellow setSupportActionBar(toolbar);
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        getAllFridges();
        //change fridge
        spinner = (Spinner) findViewById(R.id.settings_fridge_spinner);
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
        fridge1.clearFocus();
        fridge2.clearFocus();
        fridge3.clearFocus();


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

                Boolean changePrimary = false;
                Boolean changeNames = false;

                fridge_new_names.add(fridge1.getText().toString());
                fridge_new_names.add(fridge2.getText().toString());
                fridge_new_names.add(fridge3.getText().toString());
                for (Integer i = 0; i < countMyFridge; i++) {
                    if (fridge_new_names.get(i).equals("")) {
                        Toast.makeText(FridgeSettingsActivity.this, "Name can not be empty or the same as old one", Toast.LENGTH_LONG).show();
                    } else if (fridge_new_names.get(i).length() > 25) {
                        Toast.makeText(FridgeSettingsActivity.this, "Name for fridge:"+ my_fridge_list.get(i) +" is to long", Toast.LENGTH_LONG).show();
                    } else if (!fridge_new_names.get(i).equals(my_fridge_list.get(i))) {
                        updateNameFridge(my_fridge_id_list.get(i), fridge_new_names.get(i));

                        Integer index = fridge_id_list.indexOf(my_fridge_id_list.get(i));
                        my_fridge_list.set(i, fridge_new_names.get(i));
                        fridge_list.set(index, fridge_new_names.get(i));
                        changeNames = true;
                    }
                }
                fridge_new_names.clear();

                if (!primaryId_new.equals(primaryId_old)   ) {
                    updatePrimaryFridges(fridge_id_list.get(selected_fridge));

                    String pFridgeName = fridge_list.get(selected_fridge);
                    String pFridgeID = fridge_id_list.get(selected_fridge);
                    fridge_id_list.remove(selected_fridge);
                    fridge_list.remove(selected_fridge);
                    fridge_id_list.add(0, pFridgeID);
                    fridge_list.add(0, pFridgeName);
                    if (my_fridge_id_list.contains(pFridgeID)){
                        my_fridge_id_list.remove(pFridgeID);
                        my_fridge_list.remove(pFridgeName);
                        my_fridge_id_list.add(0, pFridgeID);
                        my_fridge_list.add(0, pFridgeName);
                    }

                    primaryId_old = pFridgeID;
                  changePrimary = true;
                }

                if(changeNames || changePrimary){
                    fillData();

                    Toast.makeText(FridgeSettingsActivity.this, "Saved changes", Toast.LENGTH_LONG).show();
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
        if(!fridge_id_list.isEmpty()) primaryId_new = fridge_id_list.get(selected_fridge);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void getAllFridges() {
        ProgressDialog mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Loading ...");
        mProgressDialog.show();
        Query fridges_query = FirebaseDatabase.getInstance().getReference().child("myFridges").child(userID);

        fridges_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fridge_list.clear();
                    fridge_id_list.clear();
                    my_fridge_id_list.clear();
                    my_fridge_list.clear();
                    // dataSnapshot is the "fridges" node with all children with id userID
                    for (DataSnapshot fridges : snapshot.getChildren()) {
                        MyFridge fridgeData = fridges.getValue(MyFridge.class);
                        if (!fridgeData.primary) {
                            fridge_list.add(fridgeData.name);
                            fridge_id_list.add(fridges.getKey());
                            if (fridgeData.ownerID.equals(userID)) {
                                my_fridge_id_list.add(fridges.getKey());
                                my_fridge_list.add(fridgeData.name);
                            }
                        } else {
                            primaryId_old = fridges.getKey();
                            fridge_list.add(0, fridgeData.name);
                            fridge_id_list.add(0, fridges.getKey());
                            if (fridgeData.ownerID.equals(userID)) {
                                my_fridge_id_list.add(0, fridges.getKey());
                                my_fridge_list.add(0, fridgeData.name);
                            }
                        }
                    }
                    countAllFridge = fridge_list.size();
                    countMyFridge = my_fridge_list.size();
                    fillData();
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

        if(countMyFridge > 0){
            fridge1.setVisibility(View.VISIBLE);
            fridge1.clearFocus();
            findViewById(R.id.settings_name1).setVisibility(View.VISIBLE);
            btn_del_fridge1.setEnabled(true);
            btn_del_fridge1.setVisibility(View.VISIBLE);
            btn_add_people_fridge1.setEnabled(true);
            btn_add_people_fridge1.setVisibility(View.VISIBLE);
            btn_people_fridge1.setEnabled(true);
            btn_people_fridge1.setVisibility(View.VISIBLE);
            fridge1.setText(my_fridge_list.get(0));
        }
        if(countMyFridge > 1){
            fridge2.setVisibility(View.VISIBLE);
            fridge2.clearFocus();
            findViewById(R.id.settings_name2).setVisibility(View.VISIBLE);
            btn_del_fridge2.setEnabled(true);
            btn_del_fridge2.setVisibility(View.VISIBLE);
            btn_add_people_fridge2.setEnabled(true);
            btn_add_people_fridge2.setVisibility(View.VISIBLE);
            btn_people_fridge2.setEnabled(true);
            btn_people_fridge2.setVisibility(View.VISIBLE);
            fridge2.setText(my_fridge_list.get(1));
        }
        if(countMyFridge > 2){
            fridge3.setVisibility(View.VISIBLE);
            fridge3.clearFocus();
            findViewById(R.id.settings_name3).setVisibility(View.VISIBLE);
            btn_del_fridge3.setEnabled(true);
            btn_del_fridge3.setVisibility(View.VISIBLE);
            btn_add_people_fridge3.setEnabled(true);
            btn_add_people_fridge3.setVisibility(View.VISIBLE);
            btn_people_fridge3.setEnabled(true);
            btn_people_fridge3.setVisibility(View.VISIBLE);
            fridge3.setText(my_fridge_list.get(2));

            btn_new_fridge.setVisibility(View.INVISIBLE);
            btn_new_fridge.setEnabled(false);
        }
        checkFridges();
        adapter.notifyDataSetChanged();
        spinner.setSelection(0);

        //check delete buttons
        if(btn_del_fridge1.isEnabled()) {
            btn_del_fridge1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_delete_fridge(my_fridge_id_list.get(0), 0);
                }
            });
        }
        if(btn_del_fridge2.isEnabled()) {
            btn_del_fridge2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_delete_fridge(my_fridge_id_list.get(1), 1);
                }
            });
        }

        if(btn_del_fridge3.isEnabled()) {
            btn_del_fridge3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_delete_fridge(my_fridge_id_list.get(2), 2);
                }
            });
        }

        if(btn_add_people_fridge1.isEnabled()) {
            btn_add_people_fridge1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewMember(my_fridge_id_list.get(0), my_fridge_list.get(0));
                }
            });
        }
        if(btn_add_people_fridge2.isEnabled()) {
            btn_add_people_fridge2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewMember(my_fridge_id_list.get(1), my_fridge_list.get(1));
                }
            });
        }

        if(btn_add_people_fridge3.isEnabled()) {
            btn_add_people_fridge3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewMember(my_fridge_id_list.get(2), my_fridge_list.get(2));
                }
            });
        }
        if(btn_people_fridge1.isEnabled()) {
            btn_people_fridge1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMembers(my_fridge_id_list.get(0), my_fridge_list.get(0));
                }
            });
        }
        if(btn_people_fridge2.isEnabled()) {
            btn_people_fridge2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMembers(my_fridge_id_list.get(1), my_fridge_list.get(1));
                }
            });
        }

        if(btn_people_fridge3.isEnabled()) {
            btn_people_fridge3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMembers(my_fridge_id_list.get(2), my_fridge_list.get(2));
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
        countF.setText((countMyFridge+1)+". fridge out of 3");

        // add create and cancel buttons
        builder.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String FName = fridgeName.getText().toString();
                Boolean primary = primaryFridge.isChecked();

                Fridge newFridge = new Fridge(FName, userID);
                MyFridge newMyFridge = new MyFridge(FName, userID, primary);
                User newUser = new User(Fuser.getDisplayName(), Fuser.getEmail());
                if (!FName.matches("") && FirebaseAuth.getInstance().getCurrentUser() != null && FName.length() <= 25) {
                    db.child("myFridges").child(userID).orderByChild("name").equalTo(FName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(FridgeSettingsActivity.this, "You already have a fridge with this name", Toast.LENGTH_LONG).show();
                            } else {
                                String key = FirebaseDatabase.getInstance().getReference().child("fridges").push().getKey();
                                db.child("myFridges").child(userID).child(key).setValue(newMyFridge);
                                db.child("fridgeMembers").child(key).child(userID).setValue(newUser);
                                db.child("fridges").child(key).setValue(newFridge).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(FridgeSettingsActivity.this, "Fridge created!", Toast.LENGTH_LONG).show();
                                            if(primaryFridge.isChecked()) updatePrimaryFridges(key);
                                            getAllFridges();
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

                } else if (FName.length() > 25) {
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

    private void addNewMember(String selected_fridge_id, String fridge_name) {

        String userEmail = Fuser.getEmail();

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
                MyFridge currentFridge = new MyFridge(fridge_name, userID, false);

                if (!newEmail.equals("") && newEmail.contains("@") && !newEmail.equals(userEmail)) {
                    db.child("users").orderByChild("email").equalTo(newEmail).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User newMember = new User();
                                for (DataSnapshot users : snapshot.getChildren()) {
                                    User userData = users.getValue(User.class);
                                    if (userData.email != null) {
                                        if (userData.email.equals(newEmail)) {
                                            exists = true;
                                            memberID = users.getKey();
                                            newMember = userData;
                                            memberName = newMember.name;
                                        }
                                    }
                                }
                                if (exists) {
                                    assert memberID != null;
                                    db.child("fridgeMembers").child(selected_fridge_id).child(memberID).setValue(newMember);
                                    db.child("myFridges").child(memberID).child(selected_fridge_id).setValue(currentFridge).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Notifications.sendNotificationToMembers_joinedFridge(selected_fridge_id, "New member has been added", "User "+ memberName +"has joined "+ fridge_name+" fridge", memberID, fridge_name);
                                                Toast.makeText(FridgeSettingsActivity.this, "Member added!", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(FridgeSettingsActivity.this, "Failed to create fridge! Try again.", Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });
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

    private void showMembers(String selected_fridge_id, String fridge_name) {

        String userEmail = Fuser.getEmail();


        List<String> member_name_list = new ArrayList<String>();
        List<String> member_email_list = new ArrayList<String>();
        List<String> member_id_list = new ArrayList<String>();

        Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);

        View view  = getLayoutInflater().inflate(R.layout.dialog_show_fridge_members, null);
        dialog.setContentView(view);

        TextView title = view.findViewById(R.id.dialog_show_members_title);
        title.setText("Members of "+fridge_name);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_members);
        FridgeMembersAdapter membersAdapter = new FridgeMembersAdapter(MyApplication.getAppContext(), selected_fridge_id, userID, member_id_list, member_name_list, member_email_list);
        recyclerView.setAdapter(membersAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        db.child("fridgeMembers").child(selected_fridge_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    member_name_list.clear();
                    member_email_list.clear();
                    member_id_list.clear();
                    for (DataSnapshot users : snapshot.getChildren()) {
                        User userData = users.getValue(User.class);
                        member_name_list.add(0, userData.name);
                        member_email_list.add(userData.email);
                        member_id_list.add(0, users.getKey());

                        membersAdapter.notifyDataSetChanged();
                    }

                } else {
                    Toast.makeText(FridgeSettingsActivity.this, "There is no members", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FridgeSettingsActivity.this, "Something wrong happened with fridge", Toast.LENGTH_LONG).show();
            }
        });

        dialog.show();


    }

    public void updatePrimaryFridges(String primary_fridge) {

        for(Integer i = 0; i < countAllFridge; i++) {
            HashMap<String, Object> changePrimary = new HashMap<>();
            if (fridge_id_list.get(i).equals(primary_fridge)){
                changePrimary.put("primary", true);
            } else {
                changePrimary.put("primary", false);
            }
            db.child("myFridges").child(userID).child(fridge_id_list.get(i)).updateChildren(changePrimary);

        }
    }

    private void updateNameFridge(String fridgeID, String newName) {

        HashMap<String, Object> changeName = new HashMap<>();
        changeName.put("name", newName);
        //fetch all members of this fridge
        memberID = new String();
        db.child("fridgeMembers").child(fridgeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot != null){
                    //update fridge for each member
                    for (DataSnapshot member: snapshot.getChildren()) {
                        memberID = member.getKey();
                        db.child("myFridges").child(memberID).child(fridgeID).updateChildren(changeName);
                    }
                    //when done, update fridge
                   db.child("fridges").child(fridgeID).updateChildren(changeName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG: ", databaseError.getMessage());
            }
        });


    }


    private void dialog_delete_fridge(String fridgeID, Integer fridge_order) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to delete "+ fridge_list.get(fridge_order)+" fridge").setTitle("Delete fridge");

        // add create and cancel buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete_messages_and_groceries(fridgeID);
                delete_from_users(fridgeID, my_fridge_list.get(fridge_order));

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

    private void delete_from_users(String fridgeID, String fridgeName) {
        memberID = new String();
        db.child("fridgeMembers").child(fridgeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot != null){
                    for (DataSnapshot member: snapshot.getChildren()) {
                        memberID = member.getKey();
                        db.child("myFridges").child(memberID).child(fridgeID).removeValue();
                    }
                    db.child("fridgeMembers").child(fridgeID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            db.child("fridges").child(fridgeID).removeValue();
                            Toast.makeText(FridgeSettingsActivity.this, "Deleted "+fridgeName+" fridge", Toast.LENGTH_LONG).show();
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG: ", databaseError.getMessage());
            }
        });




    }

    private void delete_messages_and_groceries(String fridgeID) {
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
        db.child("grocery").child(fridgeID).removeValue();

    }

    @Override
    public void onMemberClick(int position) {

    }
}