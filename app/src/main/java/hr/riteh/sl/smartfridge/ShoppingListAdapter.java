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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
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

import hr.riteh.sl.smartfridge.FirebaseDatabase.Grocery;
import hr.riteh.sl.smartfridge.FirebaseDatabase.GrocerySH;
import hr.riteh.sl.smartfridge.FirebaseDatabase.Message;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.GroceryViewHolder> {

    List<String> id, name, quantity, unit;
    String fridgeID;
    Context context;

    private List<String> unit_list = new ArrayList<String>();
    private String selected_unit = "";
    private OnGroceryListener mOnGroceryListener;

    public ShoppingListAdapter(Context ct, String fridge_id, List<String> gr_id, List<String> gr_name, List<String> gr_quantity, List<String> gr_unit, OnGroceryListener onGroceryListener){
        context = ct;
        fridgeID = fridge_id;
        id = gr_id;
        name = gr_name;
        quantity = gr_quantity;
        unit = gr_unit;
        this.mOnGroceryListener = onGroceryListener;
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.shopping_list_row, parent, false);
        return new GroceryViewHolder(view, mOnGroceryListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        holder.row_name.setText(name.get(position));
        holder.row_quantity.setText(quantity.get(position));
        holder.row_unit.setText(unit.get(position));
        holder.position = position;
    }


    @Override
    public int getItemCount() {
        return name.size();
    }

    public class GroceryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView row_name, row_quantity, row_unit;
        ImageButton row_bought, row_remove;
        Integer position;
        OnGroceryListener onGroceryListener;
        public GroceryViewHolder(@NonNull View itemView, OnGroceryListener onGroceryListener) {
            super(itemView);
            row_name = itemView.findViewById(R.id.recycler_grocery_name);
            row_quantity = itemView.findViewById(R.id.recycler_grocery_quantity);
            row_unit = itemView.findViewById(R.id.recycler_grocery_unit);
            row_bought = itemView.findViewById(R.id.recycler_imageButton_checked);
            row_remove = itemView.findViewById(R.id.recycler_imageButton_remove);

            row_bought.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addGrocery(id.get(position), name.get(position), Integer.parseInt(quantity.get(position)), unit.get(position));
                }
            });
            row_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteGrocery(id.get(position), name.get(position));
                      }
            });
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


    public void deleteGrocery(String groceryId, String groceryName){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage("Are you sure you want to delete "+groceryName).setTitle("Delete grocery");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseDatabase.getInstance().getReference().child("shopping_lists").child(fridgeID).child(groceryId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot grocery: snapshot.getChildren()) {
                            grocery.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

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


    public void addGrocery(String groceryId, String groceryName, Integer groceryQuantity, String groceryUnit){

        unit_list.add("kg");
        unit_list.add("g");
        unit_list.add("l");
        unit_list.add("ml");
        unit_list.add("pieces");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setCancelable(true);

        View view  = View.inflate(context, R.layout.dialog_create_grocery, null);
        builder.setView(view);


        Spinner unitSpinner = (Spinner) view.findViewById(R.id.unit_spinner);
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                    selected_unit = item.toString();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO Auto-generated method stub

            }
        });

        ArrayAdapter unitAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, unit_list);
        // Specify the layout to use when the list of choices appears
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        unitSpinner.setAdapter(unitAdapter);
        unitSpinner.setSelection(unit_list.indexOf(groceryUnit));

        EditText edt_grocery_name = (EditText) view.findViewById(R.id.dialog_grocery_name);
        //for quantity
        NumberPicker numpicker = (NumberPicker) view.findViewById(R.id.dialog_grocery_quantity_numpicker);
        numpicker.setMaxValue(1000);
        numpicker.setMinValue(1);
        //for exp date
        DatePicker datepicker = (DatePicker) view.findViewById(R.id.dialog_grocery_exp_date_datepicker);
        datepicker.setMinDate(System.currentTimeMillis() - 1000);


        TextView title = (TextView) view.findViewById(R.id.fridge_txt);
        title.setText("If you bought this grocery, save it to your fridge");
        title.setTextSize(13);

        edt_grocery_name.setText(groceryName);
        numpicker.setValue(groceryQuantity);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                numpicker.clearFocus();
                String grocery_name = edt_grocery_name.getText().toString();
                Integer quantity = numpicker.getValue();
                String exp_date = getDateFromDatePicker(datepicker);
                String ownerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Grocery msg = new Grocery(ownerID, grocery_name, quantity, selected_unit, exp_date);

                if (!grocery_name.matches("") && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    //username_textview.setText(msg.text);
                    FirebaseDatabase.getInstance().getReference().child("grocery").child(fridgeID).push().setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MyApplication.getAppContext(), "Grocery saved!", Toast.LENGTH_LONG).show();
                                DatabaseReference mPostReference =
                                        FirebaseDatabase.getInstance().getReference().child("shopping_lists").child(fridgeID).child(groceryId);
                                mPostReference.removeValue();

                            } else {

                                Toast.makeText(MyApplication.getAppContext(), "Failed to create grocery! Try again.", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                } else {
                    Toast.makeText(MyApplication.getAppContext(), "You must enter text", Toast.LENGTH_LONG).show();
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
    /**
     *
     * @param datePicker
     * @return a java.util.Date
     */
    public static String getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth()+1;
        int year =  datePicker.getYear();
        String dateString = day+"-"+month+"-"+year;
        return dateString;
    }
}
