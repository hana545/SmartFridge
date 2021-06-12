package hr.riteh.sl.smartfridge;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.commons.lang3.time.DateUtils;
import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Grocery;

public class GroceryFragment extends Fragment implements GroceryAdaper.OnGroceryListener {

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private Query groceries_query;


    GroceryAdaper groceryAdaper;
    GroceryAdaper groceryAdaperExp;
    GroceryAdaper groceryAdaperNotExp;

    RecyclerView recyclerView;

    private List<String> grocery_id_list = new ArrayList<String>();
    private List<String> grocery_list_name = new ArrayList<String>();
    private List<String> grocery_list_quantity = new ArrayList<String>();
    private List<String> grocery_list_unit = new ArrayList<String>();
    private List<String> grocery_list_exp_date = new ArrayList<String>();

    private List<String> not_exp_grocery_id_list = new ArrayList<String>();
    private List<String> not_exp_grocery_list_name = new ArrayList<String>();
    private List<String> not_exp_grocery_list_quantity = new ArrayList<String>();
    private List<String> not_exp_grocery_list_unit = new ArrayList<String>();
    private List<String> not_exp_grocery_list_exp_date = new ArrayList<String>();

    private List<String> exp_grocery_id_list = new ArrayList<String>();
    private List<String> exp_grocery_list_name = new ArrayList<String>();
    private List<String> exp_grocery_list_quantity = new ArrayList<String>();
    private List<String> exp_grocery_list_unit = new ArrayList<String>();
    private List<String> exp_grocery_list_exp_date = new ArrayList<String>();

    private List<String> unit_list = new ArrayList<String>();

    private String fridgeID;
    private String fridge_name;

    private View view;

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
            showGroceries();
        } else {
            fridgeID = "null";
            fridge_name = "null";
        }

        recyclerView = view.findViewById(R.id.recycler_view_grocery);
        groceryAdaper = new GroceryAdaper(getActivity(), grocery_list_name, grocery_list_quantity, grocery_list_unit, grocery_list_exp_date, this);
        groceryAdaperNotExp  = new GroceryAdaper(getActivity(), not_exp_grocery_list_name,not_exp_grocery_list_quantity, not_exp_grocery_list_unit, not_exp_grocery_list_exp_date, this);
        groceryAdaperExp  = new GroceryAdaper(getActivity(), exp_grocery_list_name,exp_grocery_list_quantity, exp_grocery_list_unit, exp_grocery_list_exp_date, this);
        recyclerView.setAdapter(groceryAdaper);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        MultiStateToggleButton button = (MultiStateToggleButton) view.findViewById(R.id.mstb_multi_id);
        button.setStates(new boolean[]{true, false, false});
        button.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int position) {
                switch (position) {
                    case 0:
                        recyclerView.setAdapter(groceryAdaper);
                        return;
                    case 1:
                        recyclerView.setAdapter(groceryAdaperExp);
                        return;
                    case 2:
                        recyclerView.setAdapter(groceryAdaperNotExp);
                        return;
                }
            }
        });

        FloatingActionButton add = view.findViewById(R.id.grocery_btn_newGrocery);
        add.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
               // createNewGrocery();
                // Create new fragment and transaction
                Fragment newGrocery = new NewGroceryFragment();
                //set parametar
                Bundle args = new Bundle();
                args.putString("fridgeID", fridgeID);
                args.putString("fridge_name", fridge_name);
                newGrocery.setArguments(args);
                FragmentManager ft = getFragmentManager();
                ft.beginTransaction().replace(R.id.fragment_container, newGrocery).addToBackStack(null).commit();
            }
        });
        FloatingActionButton scan = view.findViewById(R.id.grocery_btn_scanNewGrocery);
        scan.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                IntentIntegrator.forSupportFragment(GroceryFragment.this).setBeepEnabled(true).setCaptureActivity(Capture.class).setPrompt("Place barcode inside the square").initiateScan();
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult.getContents() != null){
            // Create new fragment and transaction
            Fragment newGrocery = new NewGroceryFragment();
            //set parametar
            Bundle args = new Bundle();
            args.putString("scanned_id", intentResult.getContents());
            args.putString("fridgeID", fridgeID);
            args.putString("fridge_name", fridge_name);
            newGrocery.setArguments(args);
            FragmentManager ft = getFragmentManager();
            ft.beginTransaction().replace(R.id.fragment_container, newGrocery).addToBackStack(null).commit();
        } else {
            Toast.makeText(MyApplication.getAppContext(), "Didn't scan anything", Toast.LENGTH_LONG).show();
        }
    }

    private void showGroceries(){

        groceries_query = db.child("grocery").child(fridgeID).orderByChild("grocery_name");
        groceries_query.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clean_lists();
                if (snapshot.exists()) {
                    // dataSnapshot is the "grocery" node with all children with id userID
                    for (DataSnapshot groceries : snapshot.getChildren()) {
                        Grocery groceryData = groceries.getValue(Grocery.class);
                        grocery_list_name.add(groceryData.name);
                        grocery_list_quantity.add(String.valueOf(groceryData.quantity));
                        grocery_list_unit.add(groceryData.unit);
                        grocery_list_exp_date.add(groceryData.exp_date);
                        grocery_id_list.add(groceries.getKey());
                        if (isExpired(groceryData.exp_date) || checkExpDate(groceryData.exp_date)){
                            exp_grocery_list_name.add(groceryData.name);
                            exp_grocery_list_quantity.add(String.valueOf(groceryData.quantity));
                            exp_grocery_list_unit.add(groceryData.unit);
                            exp_grocery_list_exp_date.add(groceryData.exp_date);
                            exp_grocery_id_list.add(groceries.getKey());
                        }
                        if (!isExpired(groceryData.exp_date)){
                            not_exp_grocery_list_name.add(groceryData.name);
                            not_exp_grocery_list_quantity.add(String.valueOf(groceryData.quantity));
                            not_exp_grocery_list_unit.add(groceryData.unit);
                            not_exp_grocery_list_exp_date.add(groceryData.exp_date);
                            not_exp_grocery_id_list.add(groceries.getKey());
                        }
                    }

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

    private void clean_lists() {
        grocery_id_list.clear();
        grocery_list_name.clear();
        grocery_list_quantity.clear();
        grocery_list_unit.clear();
        grocery_list_exp_date.clear();

        exp_grocery_id_list.clear();
        exp_grocery_list_name.clear();
        exp_grocery_list_quantity.clear();
        exp_grocery_list_unit.clear();
        exp_grocery_list_exp_date.clear();

        not_exp_grocery_id_list.clear();
        not_exp_grocery_list_name.clear();
        not_exp_grocery_list_quantity.clear();
        not_exp_grocery_list_unit.clear();
        not_exp_grocery_list_exp_date.clear();
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

    }

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