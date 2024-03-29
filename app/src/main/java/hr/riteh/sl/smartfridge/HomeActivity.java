package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Fridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.Grocery;
import hr.riteh.sl.smartfridge.FirebaseDatabase.GrocerySH;
import hr.riteh.sl.smartfridge.FirebaseDatabase.Message;
import hr.riteh.sl.smartfridge.FirebaseDatabase.MyFridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.Token;
import hr.riteh.sl.smartfridge.FirebaseDatabase.User;
import hr.riteh.sl.smartfridge.SendNotification.APIService;
import hr.riteh.sl.smartfridge.SendNotification.Client;
import hr.riteh.sl.smartfridge.SendNotification.Data;
import hr.riteh.sl.smartfridge.SendNotification.MyResponse;
import hr.riteh.sl.smartfridge.SendNotification.NotificationSender;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    FirebaseUser Fuser = FirebaseAuth.getInstance().getCurrentUser();
    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private Query fridges_query;

    private List<String> fridge_list = new ArrayList<String>();
    private List<String> fridge_id_list = new ArrayList<String>();
    private List<String> owner_id_list = new ArrayList<String>();

    private APIService apiService;
    private String fridgeID;
    int selected_fridge = 0;
    private String ownerID;
    long countFridge = 0;
    long countMyFridge = 0;
    String selected_unit = "";

    ArrayAdapter adapter;

    Bundle args = new Bundle();
    ProgressDialog mProgressDialog;
    MessagesFragment mMessagesFragment = new MessagesFragment();
    GroceryFragment mGroceryFragment = new GroceryFragment();
    RecipesFragment mRecipesFragment = new RecipesFragment();
    StoresFragment mStoresFragment = new StoresFragment();
    Fragment active;
    FragmentManager ft;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME); //bellow setSupportActionBar(toolbar);
        getSupportActionBar().setCustomView(R.layout.titlebar);

        getAllFridges();
        UpdateToken();
        //change fridge
        Spinner spinner = (Spinner) findViewById(R.id.fridge_spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, fridge_list);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        ft = getSupportFragmentManager();
        ft.beginTransaction().replace(R.id.fragment_container, mMessagesFragment).commit();
        active = mMessagesFragment;

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.fridge);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                fillFragmentParameters();
                mMessagesFragment.setArguments(args);
                mGroceryFragment.setArguments(args);
                mRecipesFragment.setArguments(args);
                mStoresFragment.setArguments(args);
                switch (item.getItemId()) {
                    case R.id.fridge:
                        ft.beginTransaction().replace(R.id.fragment_container, mMessagesFragment).commit();
                        active = mMessagesFragment;
                        return true;
                    case R.id.grocery:
                        ft.beginTransaction().replace(R.id.fragment_container, mGroceryFragment).commit();
                        active = mGroceryFragment;
                        return true;
                    case R.id.recipes:
                        ft.beginTransaction().replace(R.id.fragment_container, mRecipesFragment).commit();
                        active = mRecipesFragment;
                        return true;
                    case R.id.stores:
                        ft.beginTransaction().replace(R.id.fragment_container, mStoresFragment).commit();
                        active = mStoresFragment;
                        return true;

                }
                return false;
            }
        });

    }
    @Override
    protected void onRestart() {
        super.onRestart();
        getAllFridges();
    }


    private void getAllFridges() {
        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Loading ...");
        mProgressDialog.show();

        fridges_query = db.child("myFridges").child(userID);

        fridges_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fridge_list.clear();
                    fridge_id_list.clear();
                    owner_id_list.clear();
                    countMyFridge = 0;
                    // dataSnapshot is the "fridges" node with all children with id userID
                    for (DataSnapshot fridges : snapshot.getChildren()) {
                        MyFridge fridgeData = fridges.getValue(MyFridge.class);
                        if (fridgeData.ownerID.equals(userID)) countMyFridge++;
                        if (!fridgeData.primary) {
                            fridge_list.add(fridgeData.name);
                            fridge_id_list.add(fridges.getKey());
                            owner_id_list.add(fridgeData.ownerID);
                        } else {
                            fridge_list.add(0, fridgeData.name);
                            fridge_id_list.add(0, fridges.getKey());
                            owner_id_list.add(0, fridgeData.ownerID);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    countFridge = snapshot.getChildrenCount();
                    mProgressDialog.dismiss();
                    Intent intent = getIntent();
                    if (intent.getBooleanExtra("fromNotification", false)){
                        Fragment mGroceryShoppingList = new ShoppingListFragment();

                        Bundle args = new Bundle();
                        System.out.println(fridge_id_list);
                        System.out.println(fridge_list);

                        args.putString("fridgeID", fridge_id_list.get(selected_fridge));
                        args.putString("fridge_name", fridge_list.get(selected_fridge));
                        mGroceryShoppingList.setArguments(args);
                        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                        bottomNavigationView.setSelectedItemId(R.id.grocery);
                        ft.beginTransaction().replace(R.id.fragment_container, mGroceryShoppingList).addToBackStack(null).commit();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "You dont have any fridges", Toast.LENGTH_LONG).show();
                    mProgressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Something wrong happened with fridge", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void updateFridges(MyFridge newFridge, String fridgeKey) {
        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Loading ...");
        mProgressDialog.show();

        if (!newFridge.primary) {
            fridge_list.add(newFridge.name);
            fridge_id_list.add(fridgeKey);
            owner_id_list.add(newFridge.ownerID);
        } else {
            fridge_list.add(0, newFridge.name);
            fridge_id_list.add(0, fridgeKey);
            owner_id_list.add(0, newFridge.ownerID);
        }

        adapter.notifyDataSetChanged();
        countFridge = fridge_list.size();
        mProgressDialog.dismiss();

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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.options_change_my_name:
                changeUserName();
                 return true;
            case R.id.options_add_grocery_data:
                IntentIntegrator intentIntegrator = new IntentIntegrator(this);
                intentIntegrator.setBeepEnabled(true).setCaptureActivity(Capture.class).setPrompt("Place barcode inside the square").initiateScan();
                return true;
            case R.id.options_see_all_fridges:
                intent = new Intent(HomeActivity.this, SeeAllFridgesActivity.class);
                startActivity(intent);
                return true;
            case R.id.options_fridges:
                intent = new Intent(HomeActivity.this, FridgeSettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.options_newfridge:
                if (countMyFridge >= 3) {
                    Toast.makeText(this, "You already have 3 fridges", Toast.LENGTH_SHORT).show();
                } else {
                    createNewFridge();
                }
                return true;
            case R.id.options_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //change fridge - spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // On selecting a spinner item
        //String fridgeID = fridge_id_list.get(selected_fridge);
        String fridge_name = parent.getItemAtPosition(pos).toString();
        selected_fridge = (int) id;
        ownerID = owner_id_list.get(selected_fridge);

        refreshFragment();

    }

    private void fillFragmentParameters(){
        if(!fridge_id_list.isEmpty()) {
            args.putString("fridgeID", fridge_id_list.get(selected_fridge));
            args.putString("fridge_name", fridge_list.get(selected_fridge));
            args.putString("ownerID", ownerID);
        }
    }

    private void refreshFragment(){
        fillFragmentParameters();
        active.setArguments(args);
        ft = getSupportFragmentManager();
        ft.beginTransaction().detach(active).attach(active).commit();
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


///options functions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                db.child("groceries").child(result.getContents()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(MyApplication.getAppContext(), "This barcode is already saved!", Toast.LENGTH_LONG).show();
                        }
                        else {
                            addScanGroceryData(result.getContents());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MyApplication.getAppContext(), "Something wrong happened with groceries", Toast.LENGTH_LONG).show();
                    }
                });

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addScanGroceryData(String scanned_groceryID){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_create_shopping_list_grocery, null);
        builder.setView(customLayout);
        TextView fridge_title = customLayout.findViewById(R.id.fridge_txt);
        fridge_title.setText(" Add scan grocery data: ");
        TextView info = customLayout.findViewById(R.id.info);
        info.setText(" Please insert correct data for the product: ");
        List<String> unit_list = new ArrayList<String>();
        unit_list.add("kg");
        unit_list.add("g");
        unit_list.add("l");
        unit_list.add("ml");
        unit_list.add("pieces");

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
        ArrayAdapter unitAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, unit_list);
        // Specify the layout to use when the list of choices appears
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        unitSpinner.setAdapter(unitAdapter);

        EditText edt_grocery_name = (EditText) customLayout.findViewById(R.id.dialog_grocery_name);
        NumberPicker numpicker = (NumberPicker) customLayout.findViewById(R.id.dialog_grocery_quantity_numpicker);
        numpicker.setMaxValue(1000);
        numpicker.setMinValue(1);
        // add create and cancel buttons
        builder.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                numpicker.clearFocus();
                String grocery_name = edt_grocery_name.getText().toString();
                Integer quantity =  numpicker.getValue();
                String ownerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                GrocerySH msg = new GrocerySH(ownerID, grocery_name, quantity, selected_unit);
                if (!grocery_name.matches("") && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    FirebaseDatabase.getInstance().getReference().child("groceries").child(scanned_groceryID).setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MyApplication.getAppContext(), "Grocery data saved!", Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(MyApplication.getAppContext(), "Failed to save grocery data! Try again.", Toast.LENGTH_LONG).show();

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

    private void changeUserName(){

        Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);

        View view  = getLayoutInflater().inflate(R.layout.dialog_change_user_name, null);
        dialog.setContentView(view);

        EditText newName = (EditText) view.findViewById(R.id.dialog_change_name);
        newName.setText(Fuser.getDisplayName());
        TextView account = (TextView) view.findViewById(R.id.dialog_change_name_account);
        account.setText(Fuser.getEmail());
        Button btn_saveName = (Button) view.findViewById(R.id.dialog_change_name_save);

        btn_saveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!newName.getText().toString().isEmpty() && newName.getText().toString().length() < 20) {
                    HashMap<String, Object> changeName = new HashMap<>();

                    changeName.put("name", newName.getText().toString());
                    fridgeID = new String();
                    db.child("messages").orderByChild("authorID").equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot != null) {
                                for (DataSnapshot message : snapshot.getChildren()) {
                                    message.getRef().child("author").setValue(newName.getText().toString());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("TAG: ", databaseError.getMessage());
                        }
                    });
                    db.child("myFridges").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot != null) {
                                for (DataSnapshot fridge : snapshot.getChildren()) {
                                    fridgeID = fridge.getKey();
                                    db.child("fridgeMembers").child(fridgeID).child(userID).updateChildren(changeName);
                                }

                                db.child("users").child(userID).updateChildren(changeName);
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(newName.getText().toString()).build();
                                Fuser.updateProfile(profileUpdates);
                                Toast.makeText(HomeActivity.this, "Your name is updated to " + newName.getText().toString(), Toast.LENGTH_LONG).show();
                                dialog.dismiss();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("TAG: ", databaseError.getMessage());
                        }
                    });
                } else {
                    Toast.makeText(HomeActivity.this, "Error: Invalid name", Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog.show();

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
                if (!FName.matches("") && FirebaseAuth.getInstance().getCurrentUser() != null && FName.length() < 25) {
                    db.child("myFridges").child(userID).orderByChild("name").equalTo(FName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(HomeActivity.this, "You already have a fridge with this name", Toast.LENGTH_LONG).show();
                            } else {
                                String key = db.child("fridges").push().getKey();
                                db.child("fridgeMembers").child(key).child(userID).setValue(newUser);
                                db.child("myFridges").child(userID).child(key).setValue(newMyFridge);
                                db.child("fridges").child(key).setValue(newFridge).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(HomeActivity.this, "Fridge created!", Toast.LENGTH_LONG).show();
                                            if(primaryFridge.isChecked()) changePrimaryValue(key);
                                            updateFridges(newMyFridge, key);
                                            countMyFridge++;
                                            refreshFragment();
                                            // ((MessagesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getFridgeMessages(fridge_id_list.get(selected_fridge));
                                        } else {
                                            Toast.makeText(HomeActivity.this, "Failed to create fridge! Try again.", Toast.LENGTH_LONG).show();

                                        }
                                    }
                                });

                                dialog.dismiss();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                            Toast.makeText(HomeActivity.this, "Canceled", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });

                } else if (FName.length() > 25) {
                    Toast.makeText(HomeActivity.this, "Name is to long.", Toast.LENGTH_LONG).show();
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

    private void changePrimaryValue(String key) {

        fridges_query = db.child("myFridges").child(userID);

        fridges_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // dataSnapshot is the "fridges" node with all children with id userID

                    for (DataSnapshot fridges : snapshot.getChildren()) {
                        MyFridge fridgeData = fridges.getValue(MyFridge.class);
                        if (fridgeData.primary && !fridges.getKey().equals(key) ){
                            HashMap<String, Object> changePrimary = new HashMap<>();
                            changePrimary.put("primary", false);
                            db.child("myFridges").child(userID).child(fridges.getKey()).updateChildren(changePrimary);
                        }


                    }
                } else {
                    Toast.makeText(HomeActivity.this, "You dont have any fridges", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to log out ").setTitle("Log out");
        // add create and cancel buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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


    private void UpdateToken(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        db.child("tokens").child(firebaseUser.getUid()).child("token").setValue(refreshToken);

    }



}
