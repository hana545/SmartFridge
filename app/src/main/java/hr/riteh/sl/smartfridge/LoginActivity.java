package hr.riteh.sl.smartfridge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private EditText Username;
    private EditText Pass;
    private TextView Error;
    private TextView Register;
    private Button Login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Username = (EditText)findViewById(R.id.login_username);
        Pass = (EditText)findViewById(R.id.login_password);
        Login = (Button)findViewById(R.id.btn_login);
        Register = (TextView)findViewById(R.id.btn_register);
        Error = (TextView)findViewById(R.id.error_message);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate(Email.getText().toString(), Pass.getText().toString());
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
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

    }

    private void validate(String userEmail, String userPassword){
        if(userEmail.equals("Hana") && userPassword.equals("yes")) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        } else {
            Error.setText("Incorrect email or password");
        }

    }
}