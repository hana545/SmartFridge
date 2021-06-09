package hr.riteh.sl.smartfridge;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<String> text, author;
    List<String> colors = new ArrayList<String>();;
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
        colors.add("#FFE2E2");
        colors.add("#E5C1C1");
        colors.add("#F5E1C8");
        colors.add("#E7DAC9");
        colors.add("#EEE3A4");
        colors.add("#FAF4D3");
        colors.add("#EDFAD3");
        colors.add("#E3FCB1");
        colors.add("#DDE5CE");
        colors.add("#CBF3C7");
        colors.add("#E3F5E1");
        colors.add("#BDFBE3");
        colors.add("#B4E6D2");
        colors.add("#D4F3E7");
        colors.add("#C0F4F9");
        colors.add("#DAF5F8");
        colors.add("#B4DEFD");
        colors.add("#D6CDFE");
        colors.add("#E3DEFB");
        colors.add("#F4DEFB");
        colors.add("#FFDCFF");
        colors.add("#FFD1DD");
        colors.add("#FFE7ED");
        colors.add("#FCC6C6");
        colors.add("#F5F5F5");
        colors.add("#DCDCDC");
        return new MessageViewHolder(view, mOnMessageListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String shortMess;
        Random rand = new Random();
        int random_index = rand.nextInt(colors.size());
        shortMess = text.get(position);
        if (text.get(position).length() > 36) shortMess = StringUtils.substring(text.get(position), 0, 36) + "... ";
        holder.row_text.setText(shortMess);
        holder.row_author.setText("- "+author.get(position));

        //change color background
        Drawable layoutBackground = holder.layout.getBackground();
        layoutBackground = DrawableCompat.wrap(layoutBackground);
        DrawableCompat.setTint(layoutBackground,Color.parseColor(colors.get(random_index)));
        holder.layout.setBackground(layoutBackground);


    }

    @Override
    public int getItemCount() {
        return text.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView row_text, row_author;
        RelativeLayout layout;
        OnMessageListener onMessageListener;
        public MessageViewHolder(@NonNull View itemView, OnMessageListener onMessageListener) {
            super(itemView);
            row_text = itemView.findViewById(R.id.recycler_message_text);
            row_author = itemView.findViewById(R.id.recycler_message_author);
            layout = itemView.findViewById(R.id.recycler_message_relative_layout);
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
