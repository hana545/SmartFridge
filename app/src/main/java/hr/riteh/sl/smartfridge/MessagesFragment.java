package hr.riteh.sl.smartfridge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Fridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.Grocery;
import hr.riteh.sl.smartfridge.FirebaseDatabase.Message;


public class MessagesFragment extends Fragment implements MessageAdapter.OnMessageListener {

    private static MessageAdapter messageAdapter;
    private static RecyclerView recyclerView;

    private static Query mess_query;
    private DatabaseReference db =  FirebaseDatabase.getInstance().getReference();

    private static List<String> messages_list_text = new ArrayList<String>();
    private static List<String> messages_list_author= new ArrayList<String>();
    private static List<String> messages_list_authorID= new ArrayList<String>();
    private static List<String> messages_id_list = new ArrayList<String>();

    private String fridgeID;
    private String fridge_name;
    private String ownerId;

    private View view;

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_messages, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_messages);
        messageAdapter = new MessageAdapter(getActivity(), messages_list_text, messages_list_author,this);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        if (getArguments() != null){
            fridgeID = getArguments().getString("fridgeID");
            fridge_name = getArguments().getString("fridge_name");
            ownerId = getArguments().getString("ownerID");
            getFridgeMessages(fridgeID);
        } else {
            fridgeID = "null";
            fridge_name = "null";
            ownerId =  "null";
        }


////////////////

        FloatingActionButton fab = view.findViewById(R.id.home_btn_newMessage);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewMessage();
            }
        });
        return view;
    }

    public void getFridgeMessages(String fridgeID) {
        mess_query = FirebaseDatabase.getInstance().getReference().child("messages").orderByChild("fridgeID").equalTo(fridgeID);
        mess_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages_id_list.clear();
                messages_list_author.clear();
                messages_list_authorID.clear();
                messages_list_text.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot mess : snapshot.getChildren()) {
                        Message messData = mess.getValue(Message.class);
                        if (messData != null){
                            messages_list_text.add(messData.text);
                            messages_list_author.add(messData.author);
                            messages_list_authorID.add(messData.authorID);
                            messages_id_list.add(mess.getKey());
                        }
                    }
                    Collections.reverse(messages_id_list);
                    Collections.reverse(messages_list_author);
                    Collections.reverse(messages_list_authorID);
                    Collections.reverse(messages_list_text);
                    view.findViewById(R.id.text_no_messages).setVisibility(View.INVISIBLE);
                } else{
                    view.findViewById(R.id.text_no_messages).setVisibility(View.VISIBLE);
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyApplication.getAppContext(), "Something wrong happened with fridge", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createNewMessage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_create_message, null);
        builder.setView(customLayout);
        EditText message_text = (EditText) customLayout.findViewById(R.id.dialog_message_text);

        // add create and cancel buttons
        builder.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = message_text.getText().toString();
                String author_name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                String author_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String fridgeId = fridgeID;
                Message msg = new Message(text, author_name, author_id, fridgeId);

                if (!text.matches("") && text.length() < 400 && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    //username_textview.setText(msg.text);
                    FirebaseDatabase.getInstance().getReference().child("messages").push().setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                getFridgeMessages(fridgeID);
                                Toast.makeText(getActivity(), "Message posted!", Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(getActivity(), "Failed to create message! Try again.", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "You must enter text", Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onMessageClick(int position) {  ///////////////PROMJENIT DISPLAY NAME U ID!!!!!!!!!!!!
        String messageID = messages_id_list.get(position);
        final String authorID = messages_list_authorID.get(position);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(true);

        View view  = getActivity().getLayoutInflater().inflate(R.layout.dialog_show_message, null);
        dialog.setContentView(view);


        TextView textMessage = (TextView) view.findViewById(R.id.dialog_show_message_text);
        Button btn_deleteMessage = (Button) view.findViewById(R.id.dialog_delete_message);

        mess_query = db.child("messages").child(messageID);
        mess_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Message messData = snapshot.getValue(Message.class);
                if (messData != null){
                    textMessage.setText(messData.text);
                    if (authorID.equals(userId)){
                        btn_deleteMessage.setVisibility(View.VISIBLE);
                        btn_deleteMessage.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view) {
                                messages_list_text.remove(position);
                                messages_list_authorID.remove(position);
                                messages_list_author.remove(position);
                                messages_id_list.remove(position);
                                messageAdapter.notifyDataSetChanged();
                                dialog.cancel();
                            DatabaseReference mPostReference = FirebaseDatabase.getInstance().getReference()
                                    .child("messages").child(messageID);
                            mPostReference.removeValue();


                            }
                        });
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyApplication.getAppContext(), "Something wrong happened with showing message", Toast.LENGTH_LONG).show();
            }
        });


        dialog.show();
    }

}