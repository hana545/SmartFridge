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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import hr.riteh.sl.smartfridge.FirebaseDatabase.User;

public class FridgeMembersAdapter extends RecyclerView.Adapter<FridgeMembersAdapter.MemberViewHolder> {

    List<String> id, name, email;
    String fridgeID, fridge_ownerId;
    Context context;

    private OnMemberListener mOnMemberListener;

    public FridgeMembersAdapter(Context ct, String fridge_id, String fridge_owner_id, List<String> user_id, List<String> user_name, List<String> user_email){
        context = ct;
        fridgeID = fridge_id;
        fridge_ownerId = fridge_owner_id;
        id = user_id;
        name = user_name;
        email = user_email;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.member_row, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.row_name.setText(name.get(position));
        holder.position = position;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!fridge_ownerId.equals(id.get(position)) && currentUserId.equals(fridge_ownerId)){
            holder.row_remove.setVisibility(View.VISIBLE);
            holder.row_remove.setEnabled(true);
        } else {
            holder.row_remove.setVisibility(View.INVISIBLE);
            holder.row_remove.setEnabled(false);
        }
    }


    @Override
    public int getItemCount() {
        return name.size();
    }

    public interface OnMemberListener {
        void onMemberClick(int position);
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView row_name;
        Button row_remove;
        Integer position;
        OnMemberListener onMemberListener;
        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            row_name = itemView.findViewById(R.id.recycler_member_name);
            row_remove = itemView.findViewById(R.id.recycler_btn_remove);

            if(row_remove.isEnabled()) {
                row_remove.setOnClickListener(this);
            }

        }

        @Override
        public void onClick(View v) {
            removeMember(getAdapterPosition());
        }
    }



    public void removeMember(int position){
        String memberId = id.get(position);
        String memberName = name.get(position);
        DatabaseReference mPostReference = FirebaseDatabase.getInstance().getReference().child("myFridges").child(memberId).child(fridgeID);
        mPostReference.removeValue();
        mPostReference = FirebaseDatabase.getInstance().getReference().child("fridgeMembers").child(fridgeID).child(memberId);
        mPostReference.removeValue();
        name.remove(position);
        id.remove(position);
        email.remove(position);
        notifyItemRemoved(position);

        Toast.makeText(context, "Member "+memberName+" removed", Toast.LENGTH_LONG).show();

    }



}
