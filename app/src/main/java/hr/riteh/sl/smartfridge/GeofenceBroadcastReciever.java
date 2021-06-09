package hr.riteh.sl.smartfridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Toast.makeText(context, "geofence entered", Toast.LENGTH_SHORT).show();
        System.out.println("usao sam u geofence");

        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d("TAG", "onRecieve: Error receiving geofence event..");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        int transitionType = geofencingEvent.getGeofenceTransition();

        if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            notificationHelper.sendHighPriorityNotification("STORE NEARBY", "You are in vicinity of a saved store, check out your shopping list ", ShoppingListFragment.class);
        }
    }
}