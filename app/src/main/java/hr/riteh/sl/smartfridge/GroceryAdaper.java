package hr.riteh.sl.smartfridge;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class GroceryAdaper extends RecyclerView.Adapter<GroceryAdaper.GroceryViewHolder> {

    List<String> name, quantity, unit, exp_date;
    Context context;
    private OnGroceryListener mOnGroceryListener;

    public GroceryAdaper(Context ct, List<String> gr_name, List<String> gr_quantity, List<String> gr_unit, List<String> gr_exp_date, OnGroceryListener onGroceryListener){
        context = ct;
        name = gr_name;
        quantity = gr_quantity;
        unit = gr_unit;
        exp_date = gr_exp_date;
        this.mOnGroceryListener = onGroceryListener;
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.grocery_row, parent, false);
        return new GroceryViewHolder(view, mOnGroceryListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        //change color background


        holder.layout.setBackgroundResource(R.drawable.grocery_row_border);
        if (isExpired(exp_date.get(position))){
            holder.row_exp_date_title.setText("EXPIRED!");
            holder.row_exp_date.setTextColor(Color.parseColor("#FF880E0E"));
            holder.row_exp_date_title.setTextColor(Color.parseColor("#FF880E0E"));
            holder.row_name.setTextColor(Color.parseColor("#FF880E0E"));
        } else if (checkExpDate(exp_date.get(position))){
            holder.row_exp_date_title.setText("Going to expire!");
            holder.row_exp_date.setTextColor(Color.parseColor("#FF880E0E"));
            holder.row_exp_date_title.setTextColor(Color.parseColor("#FF880E0E"));
            holder.layout.setBackgroundResource(R.drawable.grocery_row_expiring);
        } else {
        }

        holder.row_name.setText(name.get(position));
        holder.row_quantity.setText(quantity.get(position));
        holder.row_unit.setText(unit.get(position));
        holder.row_exp_date.setText(exp_date.get(position));
    }

    @Override
    public int getItemCount() {
        return name.size();
    }

    public class GroceryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView row_name, row_quantity, row_unit, row_exp_date, row_exp_date_title;
        RelativeLayout layout;
        OnGroceryListener onGroceryListener;
        public GroceryViewHolder(@NonNull View itemView, OnGroceryListener onGroceryListener) {
            super(itemView);
            row_name = itemView.findViewById(R.id.recycler_grocery_name);
            row_quantity = itemView.findViewById(R.id.recycler_grocery_quantity);
            row_unit = itemView.findViewById(R.id.recycler_grocery_unit);
            row_exp_date = itemView.findViewById(R.id.recycler_grocery_exp_date);
            row_exp_date_title = itemView.findViewById(R.id.recycler_grocery_exp_date_title);
            layout = itemView.findViewById(R.id.recycler_grocery_relative_layout);
            this.onGroceryListener = onGroceryListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onGroceryListener.onGroceryClick(getAdapterPosition());
        }
    }

    public interface OnGroceryListener{
        void onGroceryClick(int position);
    }

    //if exp is 7 day away or sooner --warning
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean checkExpDate(String exp_date){
        //Getting the current date value
        LocalDate currentdate = LocalDate.now();
        int currentDay = currentdate.getDayOfMonth();
        int currentMonth = currentdate.getMonthValue();
        int currentYear = currentdate.getYear();
        String[] date_parts = exp_date.split("-");
        Integer day = Integer.valueOf(date_parts[0]);
        Integer month = Integer.valueOf(date_parts[1]);
        Integer year = Integer.valueOf(date_parts[2]);
        Date expDate = new Date(year, month, day);
        Date currDate = new Date(currentYear, currentMonth, currentDay);
        Date ExpDateRange = DateUtils.addDays(expDate, -7);
        return currDate.after(ExpDateRange) && currDate.before(expDate) || expDate.equals(currDate);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean isExpired(String exp_date){
        //Getting the current date value
        LocalDate currentdate = LocalDate.now();
        int currentDay = currentdate.getDayOfMonth();
        int currentMonth = currentdate.getMonthValue();
        int currentYear = currentdate.getYear();
        String[] date_parts = exp_date.split("-");
        Integer day = Integer.valueOf(date_parts[0]);
        Integer month = Integer.valueOf(date_parts[1]);
        Integer year = Integer.valueOf(date_parts[2]);
        Date expDate = new Date(year, month, day);
        Date currDate = new Date(currentYear, currentMonth, currentDay);
        Date ExpDateRange = DateUtils.addDays(expDate, -7);
        return currDate.after(ExpDateRange) && currDate.after(expDate);
    }
}
