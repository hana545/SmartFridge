package hr.riteh.sl.smartfridge;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StoresFragment extends Fragment {

    private String fridgeID;
    private String fridge_name;

    private View view;

    public StoresFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stores, container, false);

        if (getArguments() != null){
            fridgeID = getArguments().getString("fridgeID");
            fridge_name = getArguments().getString("fridge_name");
            //Log.i("STORESGETFRIDGE", "onCreateView: uzme argument arg="+fridgeID);
        } else {
            fridgeID = "null";
            fridge_name = "null";
            //Log.i("STORESGETFRIDGE", "onCreateView: ne uzme argument");
        }

        FloatingActionButton fab = view.findViewById(R.id.stores_btn_newStore);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}