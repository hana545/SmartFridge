package hr.riteh.sl.smartfridge;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Grocery;

public class GroceryItemFragment extends Fragment {


    ArrayAdapter unitAdapter;
    Spinner unitSpinner;
    private View view;

    private List<String> unit_list = new ArrayList<String>();
    private String selected_unit = "";

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private Query grocery_query;
    private String grocery_name;
    private String grocery_unit;
    private String grocery_exp_date;

    private String grocery_id = "";
    private String fridgeID;
    private String fridge_name;

    EditText editName;
    DatePicker datepicker;
    NumberPicker numpicker;

    public GroceryItemFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null){
            grocery_id = getArguments().getString("selected_grocery");
            fridgeID = getArguments().getString("fridgeID");
            fridge_name = getArguments().getString("fridge_name");
        } else {
            grocery_id = "";
            fridgeID = "";
            fridge_name = "";
        }
        Spinner fridge_spinner = (Spinner) getActivity().findViewById(R.id.fridge_spinner);
        fridge_spinner.setEnabled(false);
        fridge_spinner.setVisibility(View.GONE);

        unit_list.add("kg");
        unit_list.add("g");
        unit_list.add("l");
        unit_list.add("ml");
        unit_list.add("pieces");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_grocery_item, container, false);


        unitSpinner = (Spinner) view.findViewById(R.id.unit_spinner);
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
        unitAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, unit_list);
        // Specify the layout to use when the list of choices appears
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        unitSpinner.setAdapter(unitAdapter);

        fillGroceryData();

        Fragment mGroceryFragment = new GroceryFragment();
        mGroceryFragment.setArguments(getArguments());
        FragmentManager ft = getFragmentManager();

        Button save = view.findViewById(R.id.grocery_item_save);
        Button delete = view.findViewById(R.id.grocery_item_delete);

        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                editGrocery();
                ft.beginTransaction().replace(R.id.fragment_container, mGroceryFragment).addToBackStack(null).commit();
                Toast.makeText(MyApplication.getAppContext(), "Grocery updated", Toast.LENGTH_LONG).show();
            }
        });


        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                deleteGrocery();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void fillGroceryData(){
        editName = view.findViewById(R.id.grocery_item_name);
        numpicker = (NumberPicker) view.findViewById(R.id.grocery_item_quantity_numpicker);
        numpicker.setMaxValue(1000);
        numpicker.setMinValue(1);
        datepicker = view.findViewById(R.id.grocery_exp_date_datepicker);
        datepicker.setMinDate(System.currentTimeMillis() - 1000);
        //System.out.println(grocery_id);
        grocery_query = db.child("grocery").child(fridgeID).child(grocery_id);
        grocery_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Grocery groceryData = snapshot.getValue(Grocery.class);
                if(groceryData != null) {
                    grocery_name = groceryData.name;
                    grocery_unit = groceryData.unit;
                    grocery_exp_date = groceryData.exp_date;
                    String[] date_parts = grocery_exp_date.split("-");
                    Integer day = Integer.valueOf(date_parts[0]);
                    Integer month = Integer.valueOf(date_parts[1]);
                    Integer year = Integer.valueOf(date_parts[2]);
                    datepicker.updateDate(year, month, day);
                    numpicker.setValue(Integer.valueOf(groceryData.quantity));


                    editName.setText(grocery_name);
                    unitSpinner.setSelection(unit_list.indexOf(grocery_unit));

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyApplication.getAppContext(), "Something wrong happened with groceries", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void editGrocery() {
        editName = view.findViewById(R.id.grocery_item_name);
        numpicker = (NumberPicker) view.findViewById(R.id.grocery_item_quantity_numpicker);
        datepicker = view.findViewById(R.id.grocery_exp_date_datepicker);
        datepicker.setMinDate(System.currentTimeMillis() - 1000);
        grocery_query = db.child("grocery").child(fridgeID).child(grocery_id);
        grocery_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> groceryValues = new HashMap<String,Object>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    groceryValues.put(snap.getKey(), snap.getValue());
                }
                groceryValues.put("grocery_name", editName.getText().toString());
                groceryValues.put("quantity", numpicker.getValue());
                groceryValues.put("unit", selected_unit);
                groceryValues.put("exp_date",  getDateFromDatePicker(datepicker));
                db.child("grocery").child(fridgeID).child(grocery_id).updateChildren(groceryValues);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
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

    public void deleteGrocery(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Are you sure you want to delete "+grocery_name).setTitle("Delete grocery");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.child("grocery").child(fridgeID).child(grocery_id).removeValue();
                Fragment mGroceryFragment = new GroceryFragment();
                mGroceryFragment.setArguments(getArguments());
                FragmentManager ft = getFragmentManager();
                ft.beginTransaction().replace(R.id.fragment_container, mGroceryFragment).addToBackStack(null).commit();
                Toast.makeText(MyApplication.getAppContext(), "Grocery deleted", Toast.LENGTH_LONG).show();

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


}