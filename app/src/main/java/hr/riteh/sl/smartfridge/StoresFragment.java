package hr.riteh.sl.smartfridge;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Store;

public class StoresFragment extends Fragment  {

    private String fridgeID;
    private String fridge_name;
    private static RecyclerView recyclerView;
    private static Query store_query;
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private GeofencingClient geofencingClient;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;

    private static StoreAdapter storeAdapter;
    private static List<String> stores_name_text = new ArrayList<String>();
    private GoogleMap gMap;
    private Marker mark;
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
        geofencingClient = LocationServices.getGeofencingClient(this.getContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMap = googleMap;

                LatLng sydney = new LatLng(45, 14.00);
                gMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));



                store_query = db.child("stores").orderByChild("userID").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
                store_query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        stores_name_text.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot store : snapshot.getChildren()) {
                                Store storeData = store.getValue(Store.class);
                                if (storeData != null) {
                                    LatLng ll = new LatLng(storeData.lat, storeData.lng);
                                    mark = gMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(storeData.name));
                                    mark.setTag(store.getKey());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                enableUserLocation();

                gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        MarkerOptions marker = new MarkerOptions();
                        marker.position(latLng);
                        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                        mark = gMap.addMarker(marker);
                        createNewStore(latLng, mark);
                    }
                });

                gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if(marker.getTag() != null) {
                            editStore(marker);
                        }
                        return false;
                    }
                });
            }
        });

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


    private void enableUserLocation(){
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            gMap.setMyLocationEnabled(true);
        } else {
            if (shouldShowRequestPermissionRationale( Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gMap.setMyLocationEnabled(true);
            } else {

            }
        }
    }

    private void editStore(Marker marker){
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());


        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_edit_store, null);
        builder.setView(customLayout);
        EditText edt_store_name = (EditText) customLayout.findViewById(R.id.dialog_edit_store_name);
        edt_store_name.setText(marker.getTitle());

        builder.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String author_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                /*Store str = new Store(edt_store_name.getText().toString(), author_id, marker.getPosition().latitude, marker.getPosition().longitude);*/
                String edited = edt_store_name.getText().toString();
                store_query = db.child("stores").child(marker.getTag().toString());
                store_query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Object> storeValues = new HashMap<String,Object>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            storeValues.put(snap.getKey(), snap.getValue());
                        }
                        storeValues.put("name", edited);
                        db.child("stores").child(marker.getTag().toString()).updateChildren(storeValues);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                dialog.show();
                Button deleteStoreBtn = dialog.findViewById(R.id.store_delete);

                deleteStoreBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("stisnut delete");
                        deleteStore(marker);
                        dialog.dismiss();
                    }
                });
            }
        }, 1000);  // 1500 seconds


    }

    private void createNewStore(LatLng latLng, Marker marker) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());


        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_create_store, null);
        builder.setView(customLayout);
        EditText store_name = (EditText) customLayout.findViewById(R.id.dialog_store_name);

        // add create and cancel buttons
        builder.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = store_name.getText().toString();
                String author_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Store str = new Store(name, author_id, latLng.latitude, latLng.longitude);

                if (!name.matches("") && name.length() < 400 && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    FirebaseDatabase.getInstance().getReference().child("stores").push().setValue(str).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                System.out.println("Error");
                            }
                        }
                    });
                } else {
                    System.out.println("yo");
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                marker.remove();
                dialog.dismiss();
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                dialog.show();
            }
        }, 1000);  // 1500 seconds

    }

    public void deleteStore(Marker marker){
        System.out.println(marker.getTag().toString());
        store_query = db.child("stores").child(marker.getTag().toString());
        store_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot store: snapshot.getChildren()) {
                    store.getRef().removeValue();
                    marker.remove();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });

    }

}