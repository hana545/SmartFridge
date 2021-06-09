package hr.riteh.sl.smartfridge;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Grocery;


public class GroceryFragment extends Fragment implements GroceryAdaper.OnGroceryListener {

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private Query groceries_query;


    GroceryAdaper groceryAdaper;
    RecyclerView recyclerView;
    ArrayAdapter unitAdapter;

    private List<String> grocery_list_name = new ArrayList<String>();
    private List<String> grocery_list_quantity = new ArrayList<String>();
    private List<String> grocery_list_unit = new ArrayList<String>();
    private List<String> grocery_list_exp_date = new ArrayList<String>();
    private List<String> grocery_id_list = new ArrayList<String>();

    private List<String> unit_list = new ArrayList<String>();
    private String selected_unit = "";

    private String fridgeID;
    private String fridge_name;
    private String ownerId;

    private View view;

    private boolean updated_grocery;

    public GroceryFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        unit_list.add("kg");
        unit_list.add("g");
        unit_list.add("l");
        unit_list.add("ml");
        unit_list.add("pieces");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_grocery, container, false);
        Spinner fridge_spinner = (Spinner) getActivity().findViewById(R.id.fridge_spinner);
        fridge_spinner.setEnabled(true);
        fridge_spinner.setVisibility(View.VISIBLE);

        if (getArguments() != null){
            fridgeID = getArguments().getString("fridgeID");
            fridge_name = getArguments().getString("fridge_name");
            ownerId = getArguments().getString("ownerID");
            showGroceries();
        } else {
            fridgeID = "null";
            fridge_name = "null";
        }

        recyclerView = view.findViewById(R.id.recycler_view_grocery);
        groceryAdaper = new GroceryAdaper(getActivity(), grocery_list_name, grocery_list_quantity, grocery_list_unit, grocery_list_exp_date, this);
        recyclerView.setAdapter(groceryAdaper);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));



        FloatingActionButton fab = view.findViewById(R.id.grocery_btn_newGrocery);
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                createNewGrocery();
            }
        });
        ImageButton img_btn = view.findViewById(R.id.imageButton_shopping_list);
        img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create new fragment and transaction
                Fragment mGroceryShoppingList = new ShoppingListFragment();

                //((HomeActivity) getActivity()).active = mGroceryShoppingList;
                //set parametar
                Bundle args = new Bundle();
                args.putString("fridgeID", fridgeID);
                args.putString("fridge_name", fridge_name);
                mGroceryShoppingList.setArguments(args);
                FragmentManager ft = getFragmentManager();
                ft.beginTransaction().replace(R.id.fragment_container, mGroceryShoppingList).addToBackStack(null).commit();
            }
        });

        return view;
    }

    private void showGroceries(){

        groceries_query = db.child("grocery").child(fridgeID).orderByChild("grocery_name");
        groceries_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                grocery_list_exp_date.clear();
                grocery_list_name.clear();
                grocery_list_quantity.clear();
                grocery_list_unit.clear();
                grocery_id_list.clear();
                if (snapshot.exists()) {
                    // dataSnapshot is the "grocery" node with all children with id userID
                    for (DataSnapshot groceries : snapshot.getChildren()) {
                        Grocery groceryData = groceries.getValue(Grocery.class);
                        grocery_list_name.add(groceryData.grocery_name);
                        grocery_list_quantity.add(String.valueOf(groceryData.quantity));
                        grocery_list_unit.add(groceryData.unit);
                        grocery_list_exp_date.add(groceryData.exp_date);
                        grocery_id_list.add(groceries.getKey());
                    }
                    Collections.reverse(grocery_list_name);
                    Collections.reverse(grocery_list_quantity);
                    Collections.reverse(grocery_list_unit);
                    Collections.reverse(grocery_list_exp_date);
                    Collections.reverse(grocery_id_list);
                    //System.out.println("tu sam");
                    view.findViewById(R.id.text_no_groceries).setVisibility(View.INVISIBLE);

                } else {
                    view.findViewById(R.id.text_no_groceries).setVisibility(View.VISIBLE);
                }
                groceryAdaper.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyApplication.getAppContext(), "Something wrong happened with groceries", Toast.LENGTH_LONG).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNewGrocery() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_create_grocery, null);
        builder.setView(customLayout);
        TextView fridge_title = customLayout.findViewById(R.id.fridge_txt);
        fridge_title.setText("Fridge: " + fridge_name);

        Spinner unitSpinner = (Spinner) customLayout.findViewById(R.id.unit_spinner);
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

        EditText edt_grocery_name = (EditText) customLayout.findViewById(R.id.dialog_grocery_name);
       //for quantity
        NumberPicker numpicker = (NumberPicker) customLayout.findViewById(R.id.dialog_grocery_quantity_numpicker);
        numpicker.setMaxValue(1000);
        numpicker.setMinValue(1);
        //for exp date
        DatePicker datepicker = (DatePicker) customLayout.findViewById(R.id.dialog_grocery_exp_date_datepicker);
        datepicker.setMinDate(System.currentTimeMillis() - 1000);

         // add create and cancel buttons
        builder.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
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
                    Log.i("FRIDGENAME","fridge"+fridgeID);
                    FirebaseDatabase.getInstance().getReference().child("grocery").child(fridgeID).push().setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showGroceries();
                                Toast.makeText(MyApplication.getAppContext(), "Grocery saved!", Toast.LENGTH_LONG).show();

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

    @Override
    public void onGroceryClick(int position) {
        // Create new fragment and transaction
        Fragment mGroceryItemFragment = new GroceryItemFragment();
        //set parametar
        Bundle args = new Bundle();
        args.putString("selected_grocery", grocery_id_list.get(position));
        args.putString("fridgeID", fridgeID);
        args.putString("fridge_name", fridge_name);
        mGroceryItemFragment.setArguments(args);
        FragmentManager ft = getFragmentManager();
        ft.beginTransaction().replace(R.id.fragment_container, mGroceryItemFragment).addToBackStack(null).commit();


       /* Intent intent = new Intent(MyApplication.getAppContext(), GroceryItemActivity.class);
        String grocery_id = grocery_id_list.get(position);
        intent.putExtra("selected_grocery", grocery_id);
        startActivity(intent);*/
    }

}