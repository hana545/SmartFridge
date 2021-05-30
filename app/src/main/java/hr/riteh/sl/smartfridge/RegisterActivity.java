package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Fridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.MyFridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.User;

public class RegisterActivity extends AppCompatActivity {

    DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    private EditText name_edittext;
    private EditText email_edittext;
    private EditText password_edittext;
    private Button btnRegister;
    private ProgressBar progresBar;
    private TextView btnLogin;
    private FirebaseAuth mAuth;
    private ArrayList<String> fridgenames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            startActivity(intent);
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference db = database.getReference().child("users");
 
        name_edittext = (EditText)findViewById(R.id.register_username);
        email_edittext = (EditText)findViewById(R.id.register_email);
        password_edittext = (EditText)findViewById(R.id.register_password);
        btnLogin = (TextView)findViewById(R.id.btn_login);
        btnRegister = (Button)findViewById(R.id.btn_register);
        progresBar = (ProgressBar)findViewById(R.id.register_progress_bar);

        fridgenames.add("Optimistic horse");
        fridgenames.add("Charming horse");
        fridgenames.add("Ruthless horse");
        fridgenames.add("Gentle owl");
        fridgenames.add("Incredible owl");
        fridgenames.add("Charming owl");
        fridgenames.add("Gentle giraffe");
        fridgenames.add("Charming giraffe");
        fridgenames.add("Ruthless giraffe");
        fridgenames.add("Optimistic mouse");
        fridgenames.add("Incredible mouse");
        fridgenames.add("Gentle mouse");
        fridgenames.add("Optimistic rabbit");
        fridgenames.add("Ruthless rabbit");
        fridgenames.add("Incredible rabbit");

        mAuth = FirebaseAuth.getInstance();


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                registerUser();

            }
        });
    }

    private void registerUser(){
        String email = email_edittext.getText().toString();
        String name = name_edittext.getText().toString();
        String password = password_edittext.getText().toString();
        Boolean validate = true;
        if (email.isEmpty()){

            email_edittext.requestFocus();
            email_edittext.setError("Email can not be empty");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            email_edittext.requestFocus();
            email_edittext.setError("Enter valid email address");
            return;
        }
        if(name.isEmpty()) {
            name_edittext.requestFocus();
            name_edittext.setError("Username can not be empty");
            return;
        }
        if(password.isEmpty()){
            password_edittext.requestFocus();
            password_edittext.setError("Password can not be empty");
            return;
        }
        if(password.length() < 6){
            password_edittext.requestFocus();
            password_edittext.setError("Password must have at least 6 characters");
            return;
        }
        progresBar.setVisibility(View.VISIBLE);
        //register user with auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //add user to database
                        if (task.isSuccessful()) {
                            FirebaseUser Fuser = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build();

                            Fuser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("User profile", "User profile updated.");
                                            }
                                        }
                                    });
                            User user = new User(name, email);
                            FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                                    } else {
                                        progresBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(RegisterActivity.this, "Failed to register!.", Toast.LENGTH_LONG).show();

                                    }
                                }
                            });
                            //create his first fridge
                            Random rand = new Random();
                            int random_index = rand.nextInt(fridgenames.size());
                            String fridgeName = fridgenames.get(random_index);
                            String ownerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            Fridge fridge = new Fridge(fridgeName, ownerID);
                            MyFridge myfridge = new MyFridge(fridgeName, ownerID, true);
                            String fridgeKey =  db.child("fridges").push().getKey();
                            db.child("fridges").child(fridgeKey).setValue(fridge).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        db.child("fridgeMembers").child(fridgeKey).child(ownerID).setValue(user);
                                        db.child("myFridges").child(ownerID).child(fridgeKey).setValue(myfridge);
                                        Toast.makeText(RegisterActivity.this, "Fridge created!", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                    } else {
                                        progresBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(RegisterActivity.this, "Failed to create fridge! Try again.", Toast.LENGTH_LONG).show();

                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            progresBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(RegisterActivity.this, "Failed to register! Try again.", Toast.LENGTH_LONG).show();

                        }
                    }
                });

    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(HomeActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            startActivity(intent);

        }

        progresBar.setVisibility(View.INVISIBLE);
    }
}