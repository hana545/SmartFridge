package hr.riteh.sl.smartfridge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Grocery;

public class RecipesFragment extends Fragment implements RecipeAdapter.OnRecipeListener{

    private String fridgeID;
    private String fridge_name;

    private View view;

    ProgressDialog mProgressDialog;
    RecyclerView recyclerView;
    RecipeAdapter recipeAdapter;
    RecipeAdapter.OnRecipeListener list;

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private Query groceriy_names_query;

    private List<String> grocery_list_name = new ArrayList<String>();

    private static List<String> recipe_list_name = new ArrayList<String>();
    private static List<String> recipe_list_id= new ArrayList<String>();
    private static List<String> recipe_list_used_ingredients_count= new ArrayList<String>();
    private static List<String> recipe_list_used_ingredients = new ArrayList<String>();

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
        view = inflater.inflate(R.layout.fragment_recipes, container, false);
        list = this;
        if (getArguments() != null){
            fridgeID = getArguments().getString("fridgeID");
            fridge_name = getArguments().getString("fridge_name");
            //Log.i("RECIPESGETFRIDGE", "onCreateView: uzme argument arg="+fridgeID);
        } else {
            fridgeID = "null";
            fridge_name = "null";
            //Log.i("RECIPESGETFRIDGE", "onCreateView: ne uzme argument");
        }

        getGroceryNames();              ///paziti ako nema namjernica
        System.out.println("89 linija " + grocery_list_name);

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage("Loading ...");
        mProgressDialog.show();
        (new Handler()).postDelayed(this::setRecipes, 500);


        return view;
    }

    private void getGroceryNames(){

        groceriy_names_query = db.child("grocery").child(fridgeID).orderByChild("grocery_name");
        groceriy_names_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                grocery_list_name.clear();
                if (snapshot.exists()) {
                    // dataSnapshot is the "grocery" node with all children with id userID
                    for (DataSnapshot groceries : snapshot.getChildren()) {
                        Grocery groceryData = groceries.getValue(Grocery.class);
                        grocery_list_name.add(groceryData.grocery_name);
                    }
                    Collections.reverse(grocery_list_name);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyApplication.getAppContext(), "Something wrong happened with groceries", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onRecipeClick(int position) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url ="https://api.spoonacular.com/recipes/" + recipe_list_id.get(position) + "/information?includeNutrition=false";
        String apiKey = "&apiKey=3d61faada573483bb1089f304666b354";
        url = url + apiKey;

        JsonObjectRequest arrayRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                                String url = response.getString("sourceUrl");
                                System.out.println(url);
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(browserIntent);

                        } catch (Exception e){
                            System.out.println(e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.getMessage());
            }
        });

        queue.add(arrayRequest);

    }


    private void setRecipes(){
        if(grocery_list_name.size() < 5) {
            view.findViewById(R.id.text_no_recipes).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.text_no_recipes).setVisibility(View.INVISIBLE);
            ArrayList<Integer> indexes = new ArrayList<Integer>();
            for (int i = 0; i < 5; i++) {
                int randomNum = -1;
                Random rand = new Random();
                while (randomNum == -1 || indexes.contains(randomNum)){
                    randomNum = rand.nextInt((grocery_list_name.size()));
                }
                indexes.add(randomNum);
            }
            String ingredientString = "";
            for (int i = 0; i < indexes.size(); i++) {
                if (i == 0) {
                    ingredientString += grocery_list_name.get(indexes.get(i));
                } else {
                    ingredientString += ",+" + grocery_list_name.get(indexes.get(i));
                }
            }

            RequestQueue queue = Volley.newRequestQueue(getContext());
            String url ="https://api.spoonacular.com/recipes/findByIngredients?ingredients=";

            url += ingredientString;

            String urlParams = "&number=5&ranking=1";
            url += urlParams;

            String apiKey = "&apiKey=3d61faada573483bb1089f304666b354";
            url = url + apiKey;

            // Request a string response from the provided URL.
            JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                recipe_list_id.clear();
                                recipe_list_name.clear();
                                recipe_list_used_ingredients.clear();
                                recipe_list_used_ingredients_count.clear();
                                for (int i = 0; i < response.length(); i++){
                                    JSONObject recipe = response.getJSONObject(i);
                                    recipe_list_id.add(recipe.getString("id"));
                                    recipe_list_name.add(recipe.getString("title"));
                                    recipe_list_used_ingredients_count.add(recipe.getString("usedIngredientCount"));
                                    JSONArray ingredients = recipe.getJSONArray("usedIngredients");
                                    String used_ingredients = "";
                                    for (int j = 0; j < ingredients.length(); j++) {
                                        JSONObject ing = ingredients.getJSONObject(j);
                                        used_ingredients += ing.getString("name") + "  ";
                                    }
                                    recipe_list_used_ingredients.add(used_ingredients);
                                }

                                recyclerView = view.findViewById(R.id.recycler_view_recipe);
                                recipeAdapter = new RecipeAdapter(getActivity(),recipe_list_name, recipe_list_id, recipe_list_used_ingredients_count, recipe_list_used_ingredients, list);
                                recyclerView.setAdapter(recipeAdapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
                                recipeAdapter.notifyDataSetChanged();
                                mProgressDialog.dismiss();


                            } catch (Exception e){
                                System.out.println(e.getMessage());
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("jbg");
                }
            });

            queue.add(arrayRequest);



        }
    }
}