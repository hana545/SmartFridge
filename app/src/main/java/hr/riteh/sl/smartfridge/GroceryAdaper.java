package hr.riteh.sl.smartfridge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroceryAdaper extends RecyclerView.Adapter<GroceryAdaper.GroceryViewHolder> {

    List<String> name, quantity, exp_date;
    Context context;

    public GroceryAdaper(Context ct, List<String> gr_name, List<String> gr_quantity, List<String> gr_exp_date){
        context = ct;
        name = gr_name;
        quantity = gr_quantity;
        exp_date = gr_exp_date;
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.grocery_row, parent, false);
        return new GroceryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        holder.row_name.setText(name.get(position));
        holder.row_quantity.setText(quantity.get(position));
        holder.row_exp_date.setText(exp_date.get(position));
    }

    @Override
    public int getItemCount() {
        return name.size();
    }

    public class GroceryViewHolder extends RecyclerView.ViewHolder{

        TextView row_name, row_quantity, row_exp_date;

        public GroceryViewHolder(@NonNull View itemView) {
            super(itemView);
            row_name = itemView.findViewById(R.id.recycler_grocery_name);
            row_quantity = itemView.findViewById(R.id.recycler_grocery_quantity);
            row_exp_date = itemView.findViewById(R.id.recycler_grocery_exp_date);

        }
    }
}
