package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import hr.riteh.sl.smartfridge.FirebaseDatabase.MyFridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.User;

public class SeeAllFridgesActivity extends AppCompatActivity implements FridgesAdapter.OnFridgeListener {

    FirebaseUser Fuser = FirebaseAuth.getInstance().getCurrentUser();
    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();


    private static FridgesAdapter fridgeAdapter;
    private static RecyclerView recyclerView;

    private List<String> fridge_list = new ArrayList<String>();
    private List<String> fridge_id_list = new ArrayList<String>();
    private List<String> fridge_owner_list = new ArrayList<String>();
    private List<String> fridge_owner_id_list = new ArrayList<String>();

    String owner_name;
    String owner_id;

    int countAllFridge = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_fridges);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME); //bellow setSupportActionBar(toolbar);
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_fridges);
        fridgeAdapter = new FridgesAdapter(SeeAllFridgesActivity.this, fridge_id_list, fridge_list, fridge_owner_list, fridge_owner_id_list);
        recyclerView.setAdapter(fridgeAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MyApplication.getAppContext()));
        getAllFridges();


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
                    fridge_owner_list.clear();
                    fridge_owner_id_list.clear();
                    for (DataSnapshot fridges : snapshot.getChildren()) {
                        MyFridge fridgeData = fridges.getValue(MyFridge.class);
                        FirebaseDatabase.getInstance().getReference().child("users").orderByKey().equalTo(fridgeData.ownerID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot users : snapshot.getChildren()) {
                                        User userData = users.getValue(User.class);
                                        owner_name = userData.name;
                                        owner_id = users.getKey();
                                    }
                                    if (!fridgeData.primary) {
                                        fridge_list.add(fridgeData.name);
                                        fridge_id_list.add(fridges.getKey());
                                        fridge_owner_list.add(owner_name);
                                        fridge_owner_id_list.add(owner_id);
                                    } else {
                                        fridge_list.add(0, fridgeData.name);
                                        fridge_id_list.add(0, fridges.getKey());
                                        fridge_owner_list.add(0, owner_name);
                                        fridge_owner_id_list.add(0, owner_id);
                                    }

                                } else {
                                    Toast.makeText(SeeAllFridgesActivity.this, "Owner doesnt exists", Toast.LENGTH_LONG).show();
                                    mProgressDialog.dismiss();
                                }
                                countAllFridge = fridge_list.size();
                                fridgeAdapter.notifyDataSetChanged();
                                mProgressDialog.dismiss();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(SeeAllFridgesActivity.this, "Something wrong happened with fridge", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } else {
                    Toast.makeText(SeeAllFridgesActivity.this, "You dont have any fridges", Toast.LENGTH_LONG).show();
                    mProgressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SeeAllFridgesActivity.this, "Something wrong happened with fridge", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onFridgeClick(int position) {

    }
}