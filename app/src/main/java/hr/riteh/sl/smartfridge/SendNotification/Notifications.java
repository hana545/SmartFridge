package hr.riteh.sl.smartfridge.SendNotification;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Token;
import hr.riteh.sl.smartfridge.Service.MyFirebaseMessagingService;

public class Notifications {

    public static void sendNotificationToMembers_newMessage(String fridgeid, String title, String message){
        FirebaseDatabase.getInstance().getReference().child("fridgeMembers").child(fridgeid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot users : snapshot.getChildren()) {
                        if (!users.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            FirebaseDatabase.getInstance().getReference().child("tokens").child(users.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Token token = dataSnapshot.getValue(Token.class);
                                        String usertoken = token.token;
                                        MyFirebaseMessagingService.sendNotifications(usertoken, title, message);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public static void sendNotificationToMembers_joinedFridge(String fridgeid, String title, String message, String userid, String fridge_name){
        FirebaseDatabase.getInstance().getReference().child("fridgeMembers").child(fridgeid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot users : snapshot.getChildren()) {
                        if (!users.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            FirebaseDatabase.getInstance().getReference().child("tokens").child(users.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Token token = dataSnapshot.getValue(Token.class);
                                        String usertoken = token.token;
                                        if(userid.equals(users.getKey())) {
                                            MyFirebaseMessagingService.sendNotifications(usertoken, "You have been added to fridge", "You have joined "+ fridge_name+" fridge");
                                        } else {
                                            MyFirebaseMessagingService.sendNotifications(usertoken, title, message);
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}
