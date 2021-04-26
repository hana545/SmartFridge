package hr.riteh.sl.smartfridge.FirebaseDatabase;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hr.riteh.sl.smartfridge.HomeActivity;

public class User {
    public String name;
    public String email;

    public User() {

    }
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }


}
