package hr.riteh.sl.smartfridge;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecipesFragment extends Fragment {

    private String fridgeID;
    private String fridge_name;

    public RecipesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View  view = inflater.inflate(R.layout.fragment_recipes, container, false);

        if (getArguments() != null){
            fridgeID = getArguments().getString("fridgeID");
            fridge_name = getArguments().getString("fridge_name");
            //Log.i("RECIPESGETFRIDGE", "onCreateView: uzme argument arg="+fridgeID);
        } else {
            fridgeID = "null";
            fridge_name = "null";
            //Log.i("RECIPESGETFRIDGE", "onCreateView: ne uzme argument");
        }
        // Inflate the layout for this fragment
        return view;
    }
}