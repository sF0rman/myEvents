package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EventActivity extends AppCompatActivity {

    private static final String TAG = "EventActivity";

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("E, dd. MMM yyyy HH:mm", Locale.getDefault());
    private Intent intent;

    // Connectivity
    private boolean connected = true;
    private NoticeFragment noInternetWarning;

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

    private Button eventRsvpGoing;
    private Button eventRsvpMaybe;
    private Button eventAddReminder;

    private Menu contextMenu;

    private CardView rsvpCard;
    private CardView peopleCard;

    private Button showGoingBtn;
    private Button showMaybeBtn;
    private Button showInvitedBtn;

    private RecyclerView goingRv;
    private RecyclerView maybeRv;
    private RecyclerView invitedRv;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private String userId;

    // Event variables
    private String eventId;
    private String name;
    private String description;
    private Calendar start = Calendar.getInstance();
    private Calendar end = Calendar.getInstance();
    private String owner;
    private long reminderKey;
    private String reminderTime;
    private Calendar reminderCal;
    private String location;
    private String address;
    private long startInMillis;
    private long endInMillies;
    private boolean isOnline;
    private ArrayList goingPeople = new ArrayList();
    private ArrayList maybePeople = new ArrayList();
    private ArrayList invitedPeople = new ArrayList();

    private GeoPoint geoPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Lifecycle");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        noInternetWarning = new NoticeFragment(getString(R.string.error_no_internet));

        intent = getIntent();
        initUI();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: Lifecycle");
        super.onStart();
        if (isOnline()) {
            initFire();
            if (intent.hasExtra("eventId")) {
                eventId = intent.getStringExtra("eventId");
                Log.d(TAG, "onCreate: got Event:" + eventId);
                getRsvpUsers();
            } else {
                Log.e(TAG, "onCreate: No Event found");
                Toast.makeText(this, R.string.error_no_event, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
            }

            getEvent(eventId);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Lifecycle");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        contextMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");
        if (item.getItemId() == R.id.event_edit_event) {
            Intent editEvent = new Intent(this, CreateEventActivity.class);
            editEvent.putExtra("eventId", eventId);
            startActivity(editEvent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        Intent back = new Intent(this, MainActivity.class);
        startActivity(back);
        finish();
    }

    private void initUI() {
        Log.d(TAG, "initUI: ");
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

        eventRsvpGoing = findViewById(R.id.event_rsvp_going);
        eventRsvpMaybe = findViewById(R.id.event_rsvp_maybe);
        eventAddReminder = findViewById(R.id.event_add_reminder);

        rsvpCard = findViewById(R.id.event_rsvp_card);
        peopleCard = findViewById(R.id.event_people_card);

        showGoingBtn = findViewById(R.id.event_going_view);
        showMaybeBtn = findViewById(R.id.event_maybe_view);
        showInvitedBtn = findViewById(R.id.event_invited_view);

        goingRv = findViewById(R.id.event_going_recycler);
        maybeRv = findViewById(R.id.event_maybe_recycler);
        invitedRv = findViewById(R.id.event_invited_recycler);

    }

    private void adjustForOwner() {
        Log.d(TAG, "adjustForOwner: userId: " + userId + " owner: " + owner);
        if (userId.equals(owner)) {
            Log.d(TAG, "adjustForOwner: Cannot rsvp for own event");
            rsvpCard.setVisibility(View.GONE);
        } else {
            rsvpCard.setVisibility(View.VISIBLE);
        }
        if (!userId.equals(owner)) {
            Log.d(TAG, "adjustForOwner: Hide menu");
            contextMenu.getItem(0).setVisible(false);
        }
        Log.d(TAG, "adjustForOwner: " + currentUser.getUid());
    }

    private void initFire() {
        Log.d(TAG, "initFire: ");
        mAuth = FirebaseAuth.getInstance();
        try {
            currentUser = mAuth.getCurrentUser();
            userId = currentUser.getUid();
        } catch (NullPointerException e) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        db = FirebaseFirestore.getInstance();
    }

    private void getEvent(String id) {
        Log.d(TAG, "getEvent: ");
        DocumentReference eventRef = db.collection("event").document(id);
        eventRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Got document");
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "onComplete: Document data" + document.getData());

                        // Get document fields
                        try {
                            name = document.getString(Keys.NAME_KEY);
                            Log.d(TAG, "onComplete: Name: " + name);
                            description = document.getString(Keys.DESCRIPTION_KEY);
                            Log.d(TAG, "onComplete: Description: " + description);

                            owner = document.getString(Keys.OWNER_KEY);
                            Log.d(TAG, "onComplete: Owner: " + owner);


                            startInMillis = (long) document.get("start");
                            Log.d(TAG, "onComplete: startMillis: " + startInMillis);
                            endInMillies = (long) document.get("end");
                            Log.d(TAG, "onComplete: endMillis: " + endInMillies);
                            start.setTimeInMillis(startInMillis);
                            Log.d(TAG, "onComplete: Start: " + start.getTime().toString());
                            end.setTimeInMillis(endInMillies);
                            Log.d(TAG, "onComplete: End: " + end.getTime().toString());

                            isOnline = (boolean) document.get(Keys.ONLINE_KEY);
                            Log.d(TAG, "onComplete: isOnline: " + isOnline);

                            if (isOnline) {
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

                        } catch (NullPointerException e) {
                            Log.e(TAG, "onComplete: Missing field", e);
                        }


                        // Populate data
                        toolbar.setTitle(name);
                        eventName.setText(name);
                        eventDescription.setText(description);
                        eventStart.setText(dateTimeFormat.format(start.getTime()));
                        eventEnd.setText(dateTimeFormat.format(end.getTime()));

                        // Some addresses that don't contain a name use the street as location
                        // Which causes a duplicate in displaying them.
                        eventLocation.setText(location);
                        if (address.contains(location)) {
                            // strip everything up to the first comma and the following space.
                            address = address.substring(address.indexOf(",") + 2);
                        }
                        // Add line breaks
                        address = address.replaceAll(", ", "\n");


                        eventAddress.setText(address);

                        // Set map position
                        if (isOnline) {
                            mapFragment.getView().setVisibility(View.GONE);
                            onlineImg.setVisibility(View.VISIBLE);
                        } else {
                            LatLng loc = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                            mapFragment.setLocationMarkerNoAnim(loc);
                        }

                        // Set Remindertime
                        if (hasReminder(reminderKey)) {
                            Log.d(TAG, "onComplete: Has reminder with id: " + reminderKey);
                            getReminder(reminderKey);
                        } else {
                            Log.d(TAG, "onComplete: There is no reminder");
                        }

                        adjustForOwner();
                        getRsvp();

                    }
                }
            }
        });
    }

    public void setReminder(View v) {
        Log.d(TAG, "setReminder: Setting reminder!");
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

                        if (Calendar.getInstance().before(reminderCal)) {
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

    private void addReminder(final Calendar reminder) {
        Log.d(TAG, "addReminder: Adding reminder to database");
        reminderTime = dateTimeFormat.format(reminderCal.getTime());
        eventReminder.setText(reminderTime);

        DocumentReference docRef = db.collection("event")
                .document(eventId)
                .collection("invited")
                .document(currentUser.getUid());

        docRef.update("reminder", reminder.getTimeInMillis()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: Saved reminder to event");
            }
        });


        Log.d(TAG, "makeReminder: Started");

        Intent i = new Intent(getApplicationContext(), NotificationReceiver.class);
        i.putExtra("id", eventId);
        i.putExtra("reminder", reminder);
        i.putExtra("reminderTime", reminderCal.getTimeInMillis());
        i.putExtra("message", "You have an upcoming event on " + dateTimeFormat.format(start.getTime()));
        i.putExtra("name", name);
        i.putExtra("channel", "event");

        PendingIntent nIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                (int) reminderKey, i,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderCal.getTimeInMillis(), nIntent);

        Log.d(TAG, "makeReminder: reminder set for " + reminderCal.getTime());
        Log.d(TAG, "makeReminder: reminderID: " + reminder);


    }

    private boolean hasReminder(long id) {
        Log.d(TAG, "hasReminder: ");
        Intent i = new Intent(getApplicationContext(), NotificationReceiver.class);
        return PendingIntent.getBroadcast(getApplicationContext(), (int) id, i, PendingIntent.FLAG_NO_CREATE) != null;
    }

    private void getReminder(long id) {
        Log.d(TAG, "getReminder: ");
        DocumentReference reminderRef = db.collection("event")
                .document(eventId)
                .collection("invited")
                .document(userId);

        reminderRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long reminderInMillis = document.getLong("reminder");
                        Calendar rCal = Calendar.getInstance();
                        rCal.setTimeInMillis(reminderInMillis);
                        if (Calendar.getInstance().before(rCal)) {
                            eventReminder.setText(dateTimeFormat.format(rCal.getTime()));
                            eventAddReminder.setText(getString(R.string.btn_change_reminder));
                        }
                    }
                }
            }
        });
    }

    private void getRsvp() {
        Log.d(TAG, "getRsvp: ");
        final FirebaseFirestore rsvp = FirebaseFirestore.getInstance();
        rsvp.collection(Keys.EVENT_KEY)
                .document(eventId)
                .collection(Keys.INVITED_KEY)
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            String rsvpStatus = doc.getString(Keys.RSVP_KEY);
                            Log.d(TAG, "onComplete: Got RSVP status: " + rsvpStatus);
                            if (rsvpStatus.equals("going")) {
                                Log.d(TAG, "onComplete: RSVP status: Going");
                                eventRsvpGoing.setBackgroundResource(R.drawable.btn_orange);
                                eventRsvpMaybe.setBackgroundResource(R.drawable.btn_light);
                            } else if (rsvpStatus.equals("maybe")) {
                                Log.d(TAG, "onComplete: RSVP status: Maybe");
                                eventRsvpGoing.setBackgroundResource(R.drawable.btn_light);
                                eventRsvpMaybe.setBackgroundResource(R.drawable.btn_orange);
                            } else {
                                Log.d(TAG, "onComplete: RSVP status Invited");
                            }
                        }


                    }
                });
    }

    public void setGoing(View v) {
        Log.d(TAG, "setGoing: Selected");
        if (isOnline()) {
            FirebaseFirestore eventDb = FirebaseFirestore.getInstance();
            eventDb.collection("event")
                    .document(eventId)
                    .collection("invited")
                    .document(userId)
                    .update("rsvp", "going")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Set rsvp to going");
                                getRsvp();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
        getRsvpUsers();
    }

    public void setMaybe(View v) {
        Log.d(TAG, "setMaybe: Selected");
        if (isOnline()) {
            FirebaseFirestore eventDb = FirebaseFirestore.getInstance();
            eventDb.collection("event")
                    .document(eventId)
                    .collection("invited")
                    .document(userId)
                    .update("rsvp", "maybe")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Set rsvp to maybe");
                                getRsvp();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
        getRsvpUsers();
    }

    public void setNotGoing(View v) {
        if (isOnline()) {
            Log.d(TAG, "setNotGoing: Selected");
            final WarningDialogFragment warning = new WarningDialogFragment(getString(R.string.msg_warning_not_going), new WarningDialogFragment.WarningListener() {
                @Override
                public void onCompleted(boolean b) {
                    if(b){
                        final FirebaseFirestore eventDb = FirebaseFirestore.getInstance();
                        eventDb.collection("event")
                                .document(eventId)
                                .collection("invited")
                                .document(userId)
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "onComplete: Deleted reference to self in event");
                                            eventDb.collection("user")
                                                    .document(userId)
                                                    .collection("event")
                                                    .document(eventId)
                                                    .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "onComplete: Removed event from self");
                                                        Toast.makeText(EventActivity.this, "You have set not going and have removed yourself from the event!", Toast.LENGTH_SHORT).show();
                                                        Intent notGoing = new Intent(EventActivity.this, MainActivity.class);
                                                        startActivity(notGoing);
                                                        finish();
                                                    }
                                                }
                                            });


                                        }
                                    }
                                });
                    } else {
                        Log.d(TAG, "onCompleted: Cancelled");
                    }
                    

                }

                @Override
                public void onCompleted(boolean b, String email) {
                    // Do nothing
                }
            });
            warning.show(getSupportFragmentManager(), "NotGoingWarning");
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isOnline() {
        Log.d(TAG, "isOnline: ");
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        connected = (networkInfo != null && networkInfo.isConnected());
        if (!connected) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.event_notice_container, noInternetWarning)
                    .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction().remove(noInternetWarning);
        }
        return connected;
    }

    private void getRsvpUsers(){
        Log.d(TAG, "getRsvpUsers: ");

        goingPeople.clear();
        maybePeople.clear();
        invitedPeople.clear();

        // Get all users invited to event.
        FirebaseFirestore rsvpDb = FirebaseFirestore.getInstance();
        rsvpDb.collection(Keys.EVENT_KEY)
                .document(eventId)
                .collection(Keys.INVITED_KEY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot rsvpQuery : task.getResult()){
                                String eventUserId = rsvpQuery.getId();
                                String eventUserFn = rsvpQuery.getString(Keys.FIRSTNAME_KEY);
                                String eventUserSn = rsvpQuery.getString(Keys.SURNAME_KEY);
                                String eventUserEm = rsvpQuery.getString(Keys.EMAIL_KEY);
                                String eventUserIm = rsvpQuery.getString(Keys.IMAGE_KEY);
                                String eventUserRsvp = rsvpQuery.getString(Keys.RSVP_KEY);

                                User u = new User(eventUserId, eventUserFn, eventUserSn, eventUserEm, eventUserIm);
                                if(eventUserRsvp.equals("going")){
                                    goingPeople.add(u);
                                } else if(eventUserRsvp.equals("maybe")){
                                    maybePeople.add(u);
                                } else if(eventUserRsvp.equals("invited")){
                                    invitedPeople.add(u);
                                } else {
                                    Log.e(TAG, "onComplete: RSVP not found.");
                                }
                                initRecyclerView();
                            }
                        }
                    }
                });

    }

    private void initRecyclerView(){
        RecyclerView.Adapter goingAdapter = new UserAdapter(this, goingPeople);
        goingRv.setAdapter(goingAdapter);
        LinearLayoutManager goingLayoutManager = new LinearLayoutManager(this);
        goingRv.setLayoutManager(goingLayoutManager);

        RecyclerView.Adapter maybeAdapter = new UserAdapter(this, maybePeople);
        maybeRv.setAdapter(maybeAdapter);
        LinearLayoutManager maybeLayoutManager = new LinearLayoutManager(this);
        maybeRv.setLayoutManager(maybeLayoutManager);

        RecyclerView.Adapter invitedAdapter = new UserAdapter(this, invitedPeople);
        invitedRv.setAdapter(invitedAdapter);
        LinearLayoutManager invitedLayoutManager = new LinearLayoutManager(this);
        invitedRv.setLayoutManager(invitedLayoutManager);
    }

    public void showGoing(View v){
        showGoingBtn.setBackgroundResource(R.drawable.btn_orange);
        showMaybeBtn.setBackgroundResource(R.drawable.btn_light);
        showInvitedBtn.setBackgroundResource(R.drawable.btn_light);
        goingRv.setVisibility(View.VISIBLE);
        maybeRv.setVisibility(View.GONE);
        invitedRv.setVisibility(View.GONE);
    }

    public void showMaybe(View v){
        showGoingBtn.setBackgroundResource(R.drawable.btn_light);
        showMaybeBtn.setBackgroundResource(R.drawable.btn_orange);
        showInvitedBtn.setBackgroundResource(R.drawable.btn_light);
        goingRv.setVisibility(View.GONE);
        maybeRv.setVisibility(View.VISIBLE);
        invitedRv.setVisibility(View.GONE);
    }

    public void showInvited(View v){
        showGoingBtn.setBackgroundResource(R.drawable.btn_light);
        showMaybeBtn.setBackgroundResource(R.drawable.btn_light);
        showInvitedBtn.setBackgroundResource(R.drawable.btn_orange);
        goingRv.setVisibility(View.GONE);
        maybeRv.setVisibility(View.GONE);
        invitedRv.setVisibility(View.VISIBLE);
    }
}
