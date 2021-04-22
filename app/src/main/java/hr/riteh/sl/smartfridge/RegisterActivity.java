package hr.riteh.sl.smartfridge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity {

    private EditText Email;
    private EditText Pass;
    private TextView Error;
    private Button Register;
    private TextView Login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Email = (EditText)findViewById(R.id.register_email);
        Pass = (EditText)findViewById(R.id.register_password);
        Login = (TextView)findViewById(R.id.btn_login);
        Register = (Button)findViewById(R.id.btn_register);
        Error = (TextView)findViewById(R.id.error_message);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate(Email.getText().toString(), Pass.getText().toString());
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate(Email.getText().toString(), Pass.getText().toString());
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}