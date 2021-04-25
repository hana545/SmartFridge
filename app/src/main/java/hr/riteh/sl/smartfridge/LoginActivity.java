package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText Email;
    private EditText Pass;
    private TextView Register;
    private Button Login;
    private ProgressBar ProgBar;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Email = (EditText)findViewById(R.id.login_email);
        Pass = (EditText)findViewById(R.id.login_password);
        Login = (Button)findViewById(R.id.btn_login);
        ProgBar = (ProgressBar)findViewById(R.id.login_progress_bar);
        Register = (TextView)findViewById(R.id.btn_register);


        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();

            }
        });
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate(Email.getText().toString(), Pass.getText().toString());
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


    private void loginUser(){
        String email = Email.getText().toString();
        String password = Pass.getText().toString();

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

        if(password.isEmpty()){
            Pass.requestFocus();
            Pass.setError("Password can not be empty");
            return;
        }
        ProgBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    ProgBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, "Failed to login! Check you credentials", Toast.LENGTH_LONG).show();
                }
            }
        });




    }
}