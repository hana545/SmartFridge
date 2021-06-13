package hr.riteh.sl.smartfridge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {


    List<String> name, id, used_ing_count;
    List<String> used_ing;
    Context context;
    private OnRecipeListener mOnRecipeListener;

    public RecipeAdapter(Context ct, List<String> gr_name, List<String> ing_id, List<String> ing_count, List<String> ing, OnRecipeListener onRecipeListener){
        context = ct;
        name = gr_name;
        id = ing_id;
        used_ing_count = ing_count;
        used_ing = ing;
        this.mOnRecipeListener = onRecipeListener;
    }


    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recipe_row, parent, false);
        return new RecipeAdapter.RecipeViewHolder(view, mOnRecipeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.row_name.setText(name.get(position));
        holder.row_ing_used.append(used_ing.get(position));
    }



    @Override
    public int getItemCount() {
        return name.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView row_name, row_ing_used;
        OnRecipeListener onRecipeListener;
        public RecipeViewHolder(@NonNull View itemView, OnRecipeListener onRecipeListener) {
            super(itemView);
            row_name = itemView.findViewById(R.id.recycler_recipe_name);
            row_ing_used = itemView.findViewById(R.id.recycler_recipe_used);
            this.onRecipeListener = onRecipeListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onRecipeListener.onRecipeClick(getAdapterPosition());
        }
    }

    public interface OnRecipeListener{
        void onRecipeClick(int position);
    }
}
