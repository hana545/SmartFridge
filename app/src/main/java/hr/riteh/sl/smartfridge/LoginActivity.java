package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText email_edittext;
    private EditText password_edittext;
    private TextView btnRegister;
    private Button btnLogin;
    private ProgressBar progresBar;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        email_edittext = (EditText)findViewById(R.id.login_email);
        password_edittext = (EditText)findViewById(R.id.login_password);
        btnLogin = (Button)findViewById(R.id.btn_login);
        progresBar = (ProgressBar)findViewById(R.id.login_progress_bar);
        btnRegister = (TextView)findViewById(R.id.btn_register);


        mAuth = FirebaseAuth.getInstance();


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                loginUser();

            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


    private void loginUser(){
        String email = email_edittext.getText().toString();
        String password = password_edittext.getText().toString();

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

        if(password.isEmpty()){
            password_edittext.requestFocus();
            password_edittext.setError("Password can not be empty");
            return;
        }
        progresBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    progresBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, "Failed to login! Check you credentials", Toast.LENGTH_LONG).show();
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
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }
        progresBar.setVisibility(View.INVISIBLE);
    }
}