package hr.riteh.sl.smartfridge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    List<String> name;
    Context context;
    private OnStoreListener  mOnStoreListener;

    public StoreAdapter(Context ct, List<String> st_name, OnStoreListener onStoreListener){
        context = ct;
        name = st_name;
        this.mOnStoreListener = onStoreListener;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.store_row, parent, false);
        return new StoreAdapter.StoreViewHolder(view, mOnStoreListener);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreAdapter.StoreViewHolder holder, int position) {
        holder.row_name.setText(name.get(position));
    }

    @Override
    public int getItemCount() {
        return name.size();
    }

    public class StoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView row_name;
        StoreAdapter.OnStoreListener onStoreListener;
        public StoreViewHolder(@NonNull View itemView, StoreAdapter.OnStoreListener onStoreListener) {
            super(itemView);
            row_name = itemView.findViewById(R.id.recycler_store_name);
            this.onStoreListener = onStoreListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onStoreListener.onStoreClick(getAdapterPosition());
        }
    }

    public interface OnStoreListener{
        void onStoreClick(int position);
    }
}
