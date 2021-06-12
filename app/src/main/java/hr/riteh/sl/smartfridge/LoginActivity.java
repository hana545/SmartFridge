package hr.riteh.sl.smartfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Fridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.MyFridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.User;

public class LoginActivity extends AppCompatActivity {


    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private EditText email_edittext;
    private EditText password_edittext;
    private TextView btnRegister;
    private TextView btnForgotPassword;
    private Button btnLogin;
    private Button btnGoogleSignin;
    private ProgressBar progresBar;

    private boolean exists = false;

    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN=123;

    private ArrayList<String> fridgenames = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        email_edittext = (EditText)findViewById(R.id.login_email);
        password_edittext = (EditText)findViewById(R.id.login_password);
        btnLogin = (Button)findViewById(R.id.btn_login);
        btnGoogleSignin = (Button)findViewById(R.id.btn_google_signin);
        progresBar = (ProgressBar)findViewById(R.id.login_progress_bar);
        btnRegister = (TextView)findViewById(R.id.btn_register);
        btnForgotPassword = (TextView)findViewById(R.id.btn_forgot_password);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                loginUser();

            }
        });

        createGoogleRequest();

        btnGoogleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progresBar.setVisibility(View.VISIBLE);
                closeKeyboard();
                signIn();

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
        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
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

    private void createGoogleRequest() {
        // Configure Google Sign In

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.i("TESTGOOGLESIGNIN", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.i("TESTGOOGLESIGNIN", "Google sign in failed1", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            db.child("users").orderByChild("email").equalTo(user.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot users : snapshot.getChildren()) {
                                            User userData = users.getValue(User.class);
                                            if (userData.email != null) {
                                                if (userData.email.equals(user.getEmail())) {
                                                    exists = true;
                                                    progresBar.setVisibility(View.INVISIBLE);
                                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                }
                                            }
                                        }

                                    } else {
                                        User newUser = new User(user.getDisplayName(), user.getEmail());
                                        db.child("users").child(user.getUid()).setValue(newUser);
                                        //create his first fridge
                                        Random rand = new Random();
                                        int random_index = rand.nextInt(fridgenames.size());
                                        String fridgeName = fridgenames.get(random_index);
                                        String ownerID = user.getUid();

                                        Fridge fridge = new Fridge(fridgeName, ownerID);
                                        MyFridge myfridge = new MyFridge(fridgeName, ownerID, true);

                                        String fridgeKey =  db.child("fridges").push().getKey();
                                        db.child("fridges").child(fridgeKey).setValue(fridge).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    db.child("fridgeMembers").child(fridgeKey).child(ownerID).setValue(newUser);
                                                    db.child("myFridges").child(ownerID).child(fridgeKey).setValue(myfridge);
                                                    progresBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(LoginActivity.this, "Fridge created!", Toast.LENGTH_LONG).show();
                                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    progresBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(LoginActivity.this, "Failed to create fridge! Try again.", Toast.LENGTH_LONG).show();

                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            progresBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this, "Failed to sign in! Try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void resetPassword() {
        Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);

        View view  = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);
        dialog.setContentView(view);

        EditText email = (EditText) view.findViewById(R.id.dialog_reset_password_email);
        Button btn_reset = (Button) view.findViewById(R.id.dialog_reset_password);

        btn_reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String emailAddress = email.getText().toString();
                if (!emailAddress.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {

                    FirebaseAuth.getInstance().sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Check you email!", Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to reset password! Insert valid email addres", Toast.LENGTH_LONG).show();
                }
            }

        });

        dialog.show();


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
        progresBar.setVisibility(View.INVISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    }
}