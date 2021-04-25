package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import hr.riteh.sl.smartfridge.FirebaseDatabase.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText Name;
    private EditText Email;
    private EditText Pass;
    private Button Register;
    private ProgressBar ProgBar;
    private TextView Login;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference db = database.getReference().child("users");
        User user = new User();

        Name = (EditText)findViewById(R.id.register_username);
        Email = (EditText)findViewById(R.id.register_email);
        Pass = (EditText)findViewById(R.id.register_password);
        Login = (TextView)findViewById(R.id.btn_login);
        Register = (Button)findViewById(R.id.btn_register);
        ProgBar = (ProgressBar)findViewById(R.id.register_progress_bar);

        mAuth = FirebaseAuth.getInstance();


        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();

            }
        });
    }

    private void registerUser(){
        String email = Email.getText().toString();
        String name = Name.getText().toString();
        String password = Pass.getText().toString();
        Boolean validate = true;
        if (email.isEmpty()){

            Email.requestFocus();
            Email.setError("Email can not be empty");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Email.requestFocus();
            Email.setError("Enter valid email address");
            return;
        }
        if(name.isEmpty()) {
            Name.requestFocus();
            Name.setError("Username can not be empty");
            return;
        }
        if(password.isEmpty()){
            Pass.requestFocus();
            Pass.setError("Password can not be empty");
            return;
        }
        if(password.length() < 6){
            Pass.requestFocus();
            Pass.setError("Password must have at least 6 characters");
            return;
        }
        ProgBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(name, email);
                            FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                    } else {
                                        ProgBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(RegisterActivity.this, "Failed to register! Try again.", Toast.LENGTH_LONG).show();

                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            ProgBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(RegisterActivity.this, "Failed to register! Try again.", Toast.LENGTH_LONG).show();

                        }
                    }
                });

    }
}