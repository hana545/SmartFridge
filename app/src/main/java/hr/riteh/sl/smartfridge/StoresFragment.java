package hr.riteh.sl.smartfridge;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.riteh.sl.smartfridge.FirebaseDatabase.Store;

public class StoresFragment extends Fragment {

    private String fridgeID;
    private String fridge_name;
    private static RecyclerView recyclerView;
    private static Query store_query;
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private PendingIntent pendingIntent;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private float GEOFENCE_RADIUS = 100;

    private static StoreAdapter storeAdapter;
    private static List<String> stores_name_text = new ArrayList<String>();
    private GoogleMap gMap;
    private Marker mark;
    private Circle circ;
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
        Spinner fridgeSpinner = (Spinner) getActivity().findViewById(R.id.fridge_spinner);
        fridgeSpinner.setEnabled(false);
        fridgeSpinner.setVisibility(View.GONE);
        geofencingClient = LocationServices.getGeofencingClient(this.getContext());
        geofenceHelper = new GeofenceHelper(this.getContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMap = googleMap;

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
                                    /*createCircle(ll);*/
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
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                        mark = gMap.addMarker(marker);
                        circ = createCircle(latLng);
                        createNewStore(latLng, mark, circ);
                    }
                });

                gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (marker.getTag() != null) {
                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16));
                            editStore(marker, circ);
                            try {
                                removeGeofence();
                            } catch (Exception e){
                                //error
                            }
                        }
                        return false;
                    }
                });
            }
        });

        if (getArguments() != null) {
            fridgeID = getArguments().getString("fridgeID");
            fridge_name = getArguments().getString("fridge_name");
            //Log.i("STORESGETFRIDGE", "onCreateView: uzme argument arg="+fridgeID);
        } else {
            fridgeID = "null";
            fridge_name = "null";
            //Log.i("STORESGETFRIDGE", "onCreateView: ne uzme argument");
        }

        return view;
    }


    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    private Circle createCircle(LatLng latlng) {

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latlng);
        circleOptions.radius(GEOFENCE_RADIUS);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        return gMap.addCircle(circleOptions);

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


        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "You can add geofences", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Background location is neccessary for geofences to trigger", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void editStore(Marker marker, Circle circle) {
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
                        Map<String, Object> storeValues = new HashMap<String, Object>();
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
                        List<String> list = new ArrayList<String>();
                        list.add(marker.getTag().toString());
                        System.out.println(list);
                        geofencingClient.removeGeofences(list);
                        deleteStore(marker, circle);
                        removeGeofence();
                        reinstantiateGeofences();
                        dialog.dismiss();

                    }
                });
            }
        }, 1000);  // 1500 seconds


    }

    private void createNewStore(LatLng latLng, Marker marker, Circle circle) {

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
                                String id = FirebaseDatabase.getInstance().getReference().child("stores").push().getKey();
                                /*setMarkers();*/
                                if(Build.VERSION.SDK_INT >= 29) {
                                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        addGeofence(latLng, GEOFENCE_RADIUS, id);
                                    } else {
                                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                                           requestPermissions(new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION},  BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                                        } else {
                                            requestPermissions(new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION},  BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                                        }
                                    }
                                } else {
                                    addGeofence(latLng, GEOFENCE_RADIUS, id);
                                }

                            } else {
                                System.out.println("Error");
                            }
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Please insert valid store name", Toast.LENGTH_LONG).show();
                    marker.remove();
                    circle.setVisible(false);
                    circle.remove();

                }
            }
        });

        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                marker.remove();
                circle.setVisible(false);
                circle.remove();
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

    public void deleteStore(Marker marker, Circle circle) {
        System.out.println(marker.getTag().toString());
        store_query = db.child("stores").child(marker.getTag().toString());
        store_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot store : snapshot.getChildren()) {
                    store.getRef().removeValue();
                    marker.remove();
                    /*circle.setVisible(false);
                    circle.remove();*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });

    }

    @SuppressLint("MissingPermission")
    private void addGeofence(LatLng latlng, float radius, String id) {
        Geofence geofence = geofenceHelper.getGeofence(id, latlng, radius, Geofence.GEOFENCE_TRANSITION_ENTER);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        pendingIntent = geofenceHelper.getPendingIntent();
        /*if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
        geofencingClient.addGeofences(geofencingRequest, pendingIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("StoresFragment", "On Success: Geofence added");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String errorMessage = geofenceHelper.getErrorString(e);
                Log.d("StoresFragment", "onFailure: " + errorMessage);
            }
        });
    }



    public void removeGeofence() {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private PendingIntent getGeofencePendingIntent() {
        // Ne stvaraj novi PendingIntent ukoliko vec postoji:
        if (pendingIntent != null) {
            return pendingIntent;
        }

        // Sto ce se konkretno obaviti kada se detektira neka tranzicija?
        // Doticni posao obavlja broadcast receiver GeofenceBroadcastReceiver:
        Intent intent = new Intent(getContext(), GeofenceBroadcastReciever.class);

        // @Android:
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }


    private void reinstantiateGeofences() {
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
                            addGeofence(ll, GEOFENCE_RADIUS, store.getKey());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setMarkers(){
        gMap.clear();

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
                            addGeofence(ll, GEOFENCE_RADIUS, store.getKey());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTag() != null) {
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16));
                    editStore(marker, circ);
                    try {
                        removeGeofence();
                    } catch (Exception e){
                        //error
                    }
                }
                return false;
            }
        });

    }


}