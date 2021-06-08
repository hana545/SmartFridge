package hr.riteh.sl.smartfridge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Fridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.MyFridge;
import hr.riteh.sl.smartfridge.FirebaseDatabase.User;

public class FridgesAdapter extends RecyclerView.Adapter<FridgesAdapter.FridgeViewHolder> {

    List<String> id, name, owner_name, owner_id;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    Context context;

    public FridgesAdapter(Context ct, List<String> fridge_id, List<String> fridge_name, List<String> fridge_owner, List<String> fridge_owner_id){
        context = ct;
        id = fridge_id;
        name = fridge_name;
        owner_name = fridge_owner;
        owner_id = fridge_owner_id;
    }

    @NonNull
    @Override
    public FridgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fridge_row, parent, false);
        return new FridgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FridgeViewHolder holder, int position) {
        holder.row_name.setText(name.get(position));
        holder.row_owner.setText(owner_name.get(position));
        holder.position = position;

        if (!currentUserId.equals(owner_id.get(position))){
            holder.row_leave.setVisibility(View.VISIBLE);
            holder.row_leave.setEnabled(true);
        } else {
            holder.row_leave.setVisibility(View.INVISIBLE);
            holder.row_leave.setEnabled(false);
        }
    }


    @Override
    public int getItemCount() {
        return name.size();
    }


    public class FridgeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView row_name, row_owner;
        Button row_members, row_leave;
        Integer position;
        OnFridgeListener onFridgeListener;
        public FridgeViewHolder(@NonNull View itemView) {
            super(itemView);
            row_name = itemView.findViewById(R.id.recycler_fridge_name);
            row_owner = itemView.findViewById(R.id.recycler_owner_name);
            row_members = itemView.findViewById(R.id.recycler_btn_members);
            row_leave = itemView.findViewById(R.id.recycler_btn_leave);

            row_members.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMembers(id.get(position), name.get(position), owner_id.get(position));
                }
            });
            if(row_leave.isEnabled()) {
                row_leave.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            leaveFridge(getAdapterPosition());
        }
    }

    public interface OnFridgeListener{
        void onFridgeClick(int position);
    }


    public void leaveFridge(int position){
        String fridgeName = name.get(position);
        String fridgeId = id.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage("Are you sure you want to leave fridge "+fridgeName).setTitle("Leave fridge");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseDatabase.getInstance().getReference().child("myFridges").child(currentUserId).child(fridgeId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot fridge: snapshot.getChildren()) {
                            fridge.getRef().removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                FirebaseDatabase.getInstance().getReference().child("fridgeMembers").child(fridgeId).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot fridge: snapshot.getChildren()) {
                            fridge.getRef().removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                name.remove(position);
                id.remove(position);
                owner_id.remove(position);
                owner_name.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "You left fridge "+fridgeName, Toast.LENGTH_LONG).show();


            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        AlertDialog dialog = builder.create();
        dialog.show();


    }
    private void showMembers(String selected_fridge_id, String fridge_name, String fridge_owner) {

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        List<String> member_name_list = new ArrayList<String>();
        List<String> member_email_list = new ArrayList<String>();
        List<String> member_id_list = new ArrayList<String>();

        Dialog dialog = new Dialog(context);
        dialog.setCancelable(true);

        View view  = View.inflate(context, R.layout.dialog_show_fridge_members, null);
        dialog.setContentView(view);

        TextView title = view.findViewById(R.id.dialog_show_members_title);
        title.setText("Members of "+fridge_name);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_members);
        FridgeMembersAdapter membersAdapter = new FridgeMembersAdapter(MyApplication.getAppContext(), selected_fridge_id, fridge_owner, member_id_list, member_name_list, member_email_list);
        recyclerView.setAdapter(membersAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        FirebaseDatabase.getInstance().getReference().child("fridgeMembers").child(selected_fridge_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    member_name_list.clear();
                    member_email_list.clear();
                    member_id_list.clear();
                    for (DataSnapshot users : snapshot.getChildren()) {
                        User userData = users.getValue(User.class);
                        member_name_list.add(0, userData.name);
                        member_email_list.add(userData.email);
                        member_id_list.add(0, users.getKey());

                        membersAdapter.notifyDataSetChanged();
                    }

                } else {
                    Toast.makeText(context, "There is no members", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Something wrong happened with fridge", Toast.LENGTH_LONG).show();
            }
        });

        dialog.show();


    }


}
