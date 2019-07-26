package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EventActivity extends AppCompatActivity {

    public static final String TAG = "EventActivity";
    public static final String NAME_KEY = "name";
    public static final String DESCRIPTION_KEY = "description";
    public static final String GEOPONT_KEY = "geoPoint";
    public static final String LOCATION_KEY = "location";
    public static final String ADDRESS_KEY = "address";
    public static final String ONLINE_KEY = "online";
    public static final String REMINDER_KEY = "reminderKey";
    public static final String OWNER_KEY = "owner";

    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("E, dd. MMM yyyy HH:mm");
    Intent intent;

    // UI Widgets
    private MapFragment mapFragment;
    private TextView eventName;
    private TextView eventDescription;
    private TextView eventStart;
    private TextView eventEnd;
    private TextView eventLocation;
    private TextView eventAddress;
    private TextView eventReminder;
    private TextView eventOwner;

    private Button eventRsvpGoing;
    private Button eventRsvpMaybe;
    private Button eventRsvpNotGoing;
    private Button eventPplGoing;
    private Button eventPplMaybe;
    private Button eventPplInvited;
    private Button eventAddReminder;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    // Event variables
    private String eventId;
    private String name;
    private String description;
    private Calendar start = Calendar.getInstance();
    private Calendar end = Calendar.getInstance();
    private String owner;
    private long reminderKey;
    private String location;
    private String address;
    private static final String isOnlineText = "Online event";
    private long startInMillis;
    private long endInMillies;
    boolean isOnline;

    private GeoPoint geoPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        intent = getIntent();
        initFire();
        initUI();
        if(intent.hasExtra("eventId")){
            eventId = intent.getStringExtra("eventId");
            Log.d(TAG, "onCreate: got Event:" + eventId);
        } else {
            Log.e(TAG, "onCreate: No Event found");
            startActivity(new Intent(this, MainActivity.class));
        }

        getEvent(eventId);
    }

    private void initUI(){
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.event_map_fragment);
        eventName = findViewById(R.id.event_name);
        eventDescription = findViewById(R.id.event_description);
        eventStart = findViewById(R.id.event_start_time);
        eventEnd = findViewById(R.id.event_end_time);
        eventLocation = findViewById(R.id.event_location);
        eventAddress = findViewById(R.id.event_address);
        eventReminder = findViewById(R.id.event_reminder_time);
        eventOwner = findViewById(R.id.event_owner);

        eventRsvpGoing = findViewById(R.id.event_rsvp_going);
        eventRsvpMaybe = findViewById(R.id.event_rsvp_maybe);
        eventRsvpNotGoing = findViewById(R.id.event_rsvp_not_going);
        eventPplGoing = findViewById(R.id.event_ppl_going);
        eventPplMaybe = findViewById(R.id.event_ppl_maybe);
        eventPplInvited = findViewById(R.id.event_ppl_invited);
        eventAddReminder = findViewById(R.id.event_add_reminder);
    }

    private void initFire(){
        mAuth = FirebaseAuth.getInstance();
        try{
            currentUser = mAuth.getCurrentUser();
        } catch (NullPointerException e){
            startActivity(new Intent(this, LoginActivity.class));
        }
        db = FirebaseFirestore.getInstance();
    }

    private void getEvent(String id){
        DocumentReference eventRef = db.collection("event").document(id);
        eventRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: Got document");
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Log.d(TAG, "onComplete: Document data" + document.getData());

                        // Get document fields
                        try{
                            name = document.getString(NAME_KEY);
                            Log.d(TAG, "onComplete: Name: " + name);
                            description = document.getString(DESCRIPTION_KEY);
                            Log.d(TAG, "onComplete: Description: " + description);

                            owner = document.getString(OWNER_KEY);
                            Log.d(TAG, "onComplete: Owner: " + owner);

                            startInMillis = (long) document.get("start.timeInMillis");
                            Log.d(TAG, "onComplete: startMillis: " + startInMillis);
                            endInMillies = (long) document.get("end.timeInMillis");
                            Log.d(TAG, "onComplete: endMillis: " + endInMillies);
                            start.setTimeInMillis(startInMillis);
                            Log.d(TAG, "onComplete: Start: " + start.getTime().toString());
                            end.setTimeInMillis(endInMillies);
                            Log.d(TAG, "onComplete: End: " + end.getTime().toString());

                            isOnline = (boolean) document.get(ONLINE_KEY);
                            Log.d(TAG, "onComplete: isOnline: " + isOnline);

                            if(isOnline){
                                location = getString(R.string.msg_event_online);
                                geoPoint = null;
                                address = "";
                            } else {
                                location = document.getString(LOCATION_KEY);
                                Log.d(TAG, "onComplete: Location: " + location);
                                address = document.getString(ADDRESS_KEY);
                                Log.d(TAG, "onComplete: Address: " + address);
                                geoPoint = document.getGeoPoint(GEOPONT_KEY);
                                Log.d(TAG, "onComplete: GeoPount: " + geoPoint.toString());
                            }

                            reminderKey = document.getLong(REMINDER_KEY);

                        } catch (NullPointerException e){
                            Log.e(TAG, "onComplete: Missing field", e);
                        }


                        // Populate data
                        eventName.setText(name);
                        eventDescription.setText(description);
                        eventOwner.setText(owner);
                        eventStart.setText(dateTimeFormat.format(start.getTime()));
                        eventEnd.setText(dateTimeFormat.format(end.getTime()));

                        // Some addresses that don't contain a name use the street as location
                        // Which causes a duplicate in displaying them.
                        eventLocation.setText(location);
                        if(address.contains(location)){
                            // strip everything up to the first comma and the following space.
                            address = address.substring(address.indexOf(",")+2);
                        }
                        // Add line breaks
                        address = address.replaceAll(", ", "\n");


                        eventAddress.setText(address);

                        // Set map position
                        if(isOnline){
                            mapFragment.getView().setVisibility(View.GONE);
                        } else {
                            LatLng loc = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                            mapFragment.setLocationMarker(loc);
                        }

                        // Set Remindertime
                        if(hasReminder(reminderKey)) {
                            Log.d(TAG, "onComplete: Has reminder with id: " + reminderKey);
                            getReminder(reminderKey);
                        } else {
                            Log.d(TAG, "onComplete: There is no reminder");
                        }

                    }
                }
            }
        });
    }

    private boolean hasReminder(long id){
        Intent i = new Intent(getApplicationContext(), NotificationReceiver.class);
        return PendingIntent.getBroadcast(getApplicationContext(), (int)id, i, PendingIntent.FLAG_NO_CREATE) != null;
    }

    private void getReminder(long id){
        Intent i = new Intent(getApplicationContext(), NotificationReceiver.class);
        Log.d(TAG, "getReminder: " + PendingIntent.getBroadcast(getApplicationContext(),(int)id, i, PendingIntent.FLAG_NO_CREATE).toString());
    }
}
