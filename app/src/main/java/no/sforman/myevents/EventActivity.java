package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class EventActivity extends AppCompatActivity {

    public static final String TAG = "EventActivity";

    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("E, dd. MMM yyyy HH:mm");
    Intent intent;

    // UI Widgets
    private MapFragment mapFragment;
    private FrameLayout onlineImg;

    private Toolbar toolbar;

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

    private Menu contextMenu;

    private CardView rsvpCard;
    private CardView peopleCard;

    private RecyclerView goingRv;
    private RecyclerView maybeRv;
    private RecyclerView invitedRv;

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
    String reminderTime;
    Calendar reminderCal;
    private String location;
    private String address;
    private static final String isOnlineText = "Online event";
    private long startInMillis;
    private long endInMillies;
    boolean isOnline;
    ArrayList goingPeople = new ArrayList();
    ArrayList maybePeople = new ArrayList();
    ArrayList invitedPeople = new ArrayList();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        contextMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.event_edit_event){
            Intent editEvent = new Intent(this, CreateEventActivity.class);
            editEvent.putExtra("eventId", eventId);
            startActivity(editEvent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent back = new Intent(this, MainActivity.class);
        startActivity(back);
        finish();
    }

    private void initUI(){
        toolbar = findViewById(R.id.event_toolbar);
        setSupportActionBar(toolbar);
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.event_map_fragment);
        onlineImg = findViewById(R.id.event_online_image);
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

        rsvpCard = findViewById(R.id.event_rsvp_card);
        peopleCard = findViewById(R.id.event_people_card);


    }

    private void adjustForOwner(String userId){
        Log.d(TAG, "adjustForOwner: userId: " + userId + " owner: " + owner);
        if(userId.equals(owner)){
            Log.d(TAG, "adjustForOwner: Cannot rsvp for own event");
            rsvpCard.setVisibility(View.GONE);
        }
        if(!userId.equals(owner)){
            Log.d(TAG, "adjustForOwner: Hide menu");
            contextMenu.getItem(0).setVisible(false);
        }
        if(goingPeople.isEmpty() && maybePeople.isEmpty() && invitedPeople.isEmpty()){
            peopleCard.setVisibility(View.GONE);
        }
        Log.d(TAG, "adjustForOwner: " + currentUser.getUid());
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
                            name = document.getString(Keys.NAME_KEY);
                            Log.d(TAG, "onComplete: Name: " + name);
                            description = document.getString(Keys.DESCRIPTION_KEY);
                            Log.d(TAG, "onComplete: Description: " + description);

                            owner = document.getString(Keys.OWNER_KEY);
                            Log.d(TAG, "onComplete: Owner: " + owner);

                            startInMillis = (long) document.get("start.timeInMillis");
                            Log.d(TAG, "onComplete: startMillis: " + startInMillis);
                            endInMillies = (long) document.get("end.timeInMillis");
                            Log.d(TAG, "onComplete: endMillis: " + endInMillies);
                            start.setTimeInMillis(startInMillis);
                            Log.d(TAG, "onComplete: Start: " + start.getTime().toString());
                            end.setTimeInMillis(endInMillies);
                            Log.d(TAG, "onComplete: End: " + end.getTime().toString());

                            isOnline = (boolean) document.get(Keys.ONLINE_KEY);
                            Log.d(TAG, "onComplete: isOnline: " + isOnline);

                            if(isOnline){
                                location = getString(R.string.msg_event_online);
                                geoPoint = null;
                                address = "";
                            } else {
                                location = document.getString(Keys.LOCATION_KEY);
                                Log.d(TAG, "onComplete: Location: " + location);
                                address = document.getString(Keys.ADDRESS_KEY);
                                Log.d(TAG, "onComplete: Address: " + address);
                                geoPoint = document.getGeoPoint(Keys.GEOPONT_KEY);
                                Log.d(TAG, "onComplete: GeoPount: " + geoPoint.toString());
                            }

                            reminderKey = document.getLong(Keys.REMINDER_KEY);

                        } catch (NullPointerException e){
                            Log.e(TAG, "onComplete: Missing field", e);
                        }


                        // Populate data
                        toolbar.setTitle(name);
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
                            onlineImg.setVisibility(View.VISIBLE);
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

                        adjustForOwner(mAuth.getUid());

                    }
                }
            }
        });
    }

    public void setReminder(View v){
        reminderCal = Calendar.getInstance();
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setOnDateChosenListener(new DatePickerFragment.OnDateChosenListener() {
            @Override
            public void onDateChosen(int year, int month, int day) {
                reminderCal.set(year, month, day);

                TimePickerFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.setOnTimeChosenListener(new TimePickerFragment.OnTimeChosenListener() {
                    @Override
                    public void onTimeChosen(int hour, int min) {
                        reminderCal.set(Calendar.HOUR_OF_DAY, hour);
                        reminderCal.set(Calendar.MINUTE, min);
                        reminderCal.set(Calendar.SECOND, 0);
                        reminderCal.set(Calendar.MILLISECOND, 0);

                        if(Calendar.getInstance().before(reminderCal)){
                            addReminder(reminderCal);
                        } else {
                            Toast.makeText(EventActivity.this, "You cannot set reminder before current date!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                timePickerFragment.show(getSupportFragmentManager(), "TimePicker");

            }
        });
        datePickerFragment.show(getSupportFragmentManager(), "DatePicker");



    }

    private void addReminder(final Calendar reminder){

        reminderTime = dateTimeFormat.format(reminderCal.getTime());
        eventReminder.setText(reminderTime);

        DocumentReference docRef = db.collection("event")
                .document(eventId)
                .collection("Going")
                .document(currentUser.getUid());
        
        docRef.update("reminder", reminder).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: Saved reminder to event");
            }
        });


        Log.d(TAG, "makeReminder: Started");

        Intent i = new Intent (getApplicationContext(), NotificationReceiver.class);
        i.putExtra("id", eventId);
        i.putExtra("reminder", reminder);
        i.putExtra("reminderTime", reminderCal.getTimeInMillis());
        i.putExtra("message", "You have an upcoming event on " + dateTimeFormat.format(start.getTime()));
        i.putExtra("name", name);
        i.putExtra("channel", "event");

        PendingIntent nIntent = (PendingIntent) PendingIntent.getBroadcast(
                getApplicationContext(),
                (int)reminderKey, i,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderCal.getTimeInMillis(), nIntent);

        Log.d(TAG, "makeReminder: reminder set for " + reminderCal.getTime());
        Log.d(TAG, "makeReminder: reminderID: " + reminder);


    }

    private boolean hasReminder(long id){
        Intent i = new Intent(getApplicationContext(), NotificationReceiver.class);
        return PendingIntent.getBroadcast(getApplicationContext(), (int)id, i, PendingIntent.FLAG_NO_CREATE) != null;
    }

    private void getReminder(long id){
        DocumentReference reminderRef = db.collection("event")
                .document(eventId)
                .collection("Going")
                .document(currentUser.getUid());

        reminderRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        long reminderInMillis = document.getLong("reminder.timeInMillis");
                        Calendar rCal = Calendar.getInstance();
                        rCal.setTimeInMillis(reminderInMillis);
                        if(Calendar.getInstance().before(rCal)){
                            eventReminder.setText(dateTimeFormat.format(rCal.getTime()));
                            eventAddReminder.setText(R.string.btn_change_reminder);
                        }
                    }
                }
            }
        });
    }
}
