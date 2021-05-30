package hr.riteh.sl.smartfridge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<String> text, author;
    Context context;
    private OnMessageListener mOnMessageListener;

    public MessageAdapter(Context ct, List<String> mess_text, List<String> mess_author, OnMessageListener onMessageListener){
        context = ct;
        text = mess_text;
        author = mess_author;
        this.mOnMessageListener = onMessageListener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.message_row, parent, false);
        return new MessageViewHolder(view, mOnMessageListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String shortMess;
        shortMess = text.get(position);
        if (text.get(position).length() > 36) shortMess = StringUtils.substring(text.get(position), 0, 36) + "... ";
        holder.row_text.setText(shortMess);
        holder.row_author.setText("- "+author.get(position));
    }

    @Override
    public int getItemCount() {
        return text.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView row_text, row_author;
        OnMessageListener onMessageListener;
        public MessageViewHolder(@NonNull View itemView, OnMessageListener onMessageListener) {
            super(itemView);
            row_text = itemView.findViewById(R.id.recycler_message_text);
            row_author = itemView.findViewById(R.id.recycler_message_author);
            this.onMessageListener = onMessageListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onMessageListener.onMessageClick(getAdapterPosition());
        }
    }

    public interface OnMessageListener{
        void onMessageClick(int position);
    }
}
