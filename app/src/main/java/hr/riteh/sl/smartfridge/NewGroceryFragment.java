package hr.riteh.sl.smartfridge;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class NewGroceryFragment extends Fragment {


    ArrayAdapter unitAdapter;
    Spinner unitSpinner;
    private View view;

    private List<String> unit_list = new ArrayList<String>();
    private String selected_unit = "";

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    private String scanned_groceryID = "";
    private String fridgeID;
    private String fridge_name;
    ProgressDialog mProgressDialog;

    EditText edt_grocery_name;
    NumberPicker numpicker;
    DatePicker datepicker;
    TextView grocery_info;

    GrocerySH scannedGrocery;


    public NewGroceryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null){
            scanned_groceryID = getArguments().getString("scanned_id");
            fridgeID = getArguments().getString("fridgeID");
            fridge_name = getArguments().getString("fridge_name");
        } else {
            scanned_groceryID = "";
            fridgeID = "";
            fridge_name = "";
        }
        if (scanned_groceryID != null)findScannedGrocery(scanned_groceryID);
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
        view = inflater.inflate(R.layout.fragment_new_grocery, container, false);

        TextView fridge_title = view.findViewById(R.id.fridge_txt);
        fridge_title.setText("Fridge: " + fridge_name);

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

        grocery_info = (TextView) view.findViewById(R.id.new_grocery_info);
        grocery_info.setVisibility(View.GONE);
        edt_grocery_name = (EditText) view.findViewById(R.id.new_grocery_name);
        edt_grocery_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeKeyboard();
                }
            }
        });
        //for quantity
        numpicker = (NumberPicker) view.findViewById(R.id.new_grocery_quantity_numpicker);
        numpicker.setMaxValue(1000);
        numpicker.setMinValue(1);
        //for exp date
        datepicker = (DatePicker) view.findViewById(R.id.new_grocery_exp_date_datepicker);
        datepicker.setMinDate(System.currentTimeMillis() - 1000);

        Button create = (Button) view.findViewById(R.id.new_grocery_btn_create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGrocery();
            }
        });

        // Inflate the layout for this fragment
        return view;
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

    private void findScannedGrocery(String scanned_groceryID) {
        mProgressDialog = new ProgressDialog(getActivity());

        mProgressDialog.setMessage("Loading ...");
        mProgressDialog.show();

        db.child("groceries").child(scanned_groceryID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    scannedGrocery = snapshot.getValue(GrocerySH.class);
                    fillGroceryData();
                    mProgressDialog.dismiss();

                }
                else {
                    grocery_info.setVisibility(View.VISIBLE);
                    grocery_info.setText("Couldn't find any matching data. Create new one: ");
                    mProgressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyApplication.getAppContext(), "Something wrong happened with groceries", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fillGroceryData() {
        grocery_info.setVisibility(View.VISIBLE);
        grocery_info.setText("Found grocery data: ");

        numpicker.setValue(Integer.valueOf(scannedGrocery.quantity));

        edt_grocery_name.setText(scannedGrocery.name);

        unitSpinner.setSelection(unit_list.indexOf(scannedGrocery.unit));

    }

    private void createGrocery(){
        numpicker.clearFocus();
        String grocery_name = edt_grocery_name.getText().toString();
        Integer quantity = numpicker.getValue();
        String exp_date = getDateFromDatePicker(datepicker);
        String ownerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Grocery msg = new Grocery(ownerID, grocery_name, quantity, selected_unit, exp_date);

        if (!grocery_name.matches("") && FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase.getInstance().getReference().child("grocery").child(fridgeID).push().setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Fragment mGroceryFragment = new GroceryFragment();
                        mGroceryFragment.setArguments(getArguments());
                        FragmentManager ft = getFragmentManager();
                        ft.beginTransaction().replace(R.id.fragment_container, mGroceryFragment).addToBackStack(null).commit();
                        Toast.makeText(MyApplication.getAppContext(), "Grocery created!", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(MyApplication.getAppContext(), "Failed to create grocery! Try again.", Toast.LENGTH_LONG).show();

                    }
                }
            });

        } else {
            Toast.makeText(MyApplication.getAppContext(), "You must enter text", Toast.LENGTH_LONG).show();
        }
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}