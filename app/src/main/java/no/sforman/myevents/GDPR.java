package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

public class GDPR extends AppCompatActivity {

    private static final String TAG = "GDPR";

    private TextView id;
    private TextView name;
    private TextView email;
    private TextView image;

    private RecyclerView events;
    private RecyclerView invited;
    private RecyclerView contacts;

    private ArrayList<User> contactList = new ArrayList<>();
    private ArrayList<Event> eventList = new ArrayList<>();
    private ArrayList<Event> invitedList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Lifecycle");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdpr);
        initUI();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            userId = currentUser.getUid();
        } else {
            Intent noUser = new Intent(this, LoginActivity.class);
            startActivity(noUser);
            finish();
        }


    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG, "onStart: Lifecycle");
        getAllData();
    }

    private void initUI(){
        Log.d(TAG, "initUI: ");
        id = findViewById(R.id.gdpr_user_id);
        name = findViewById(R.id.gdpr_name);
        email = findViewById(R.id.gdpr_email);
        image = findViewById(R.id.gdpr_image);

        events = findViewById(R.id.gdpr_events);
        invited = findViewById(R.id.gdpr_invited);
        contacts = findViewById(R.id.gdpr_contacts);
    }

    private void getAllData(){
        Log.d(TAG, "getAllData: ");

        contactList.clear();
        eventList.clear();
        invitedList.clear();

        FirebaseFirestore self = FirebaseFirestore.getInstance();
        self.collection(Keys.USER_KEY)
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: Got self");
                            DocumentSnapshot u = task.getResult();
                            id.setText(userId);
                            String fullname = u.getString(Keys.FIRSTNAME_KEY) + " " + u.getString(Keys.SURNAME_KEY);
                            name.setText(fullname);
                            email.setText(u.getString(Keys.EMAIL_KEY));
                            image.setText(u.getString(Keys.IMAGE_KEY));
                        }
                    }
                });

        FirebaseFirestore events = FirebaseFirestore.getInstance();
        events.collection(Keys.USER_KEY)
                .document(userId)
                .collection(Keys.EVENT_KEY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot e : task.getResult()){
                                Log.d(TAG, "onComplete: event" + e.toString());
                                String id = e.getId();
                                String name = e.getString(Keys.NAME_KEY);
                                String owner = e.getString(Keys.OWNER_KEY);
                                String description = e.getString(Keys.DESCRIPTION_KEY);
                                Calendar start = Calendar.getInstance();
                                Calendar end = Calendar.getInstance();
                                long stime = e.getLong("start");
                                long etime = e.getLong("end");
                                start.setTimeInMillis(stime);
                                end.setTimeInMillis(etime);
                                GeoPoint geoPoint = e.getGeoPoint(Keys.GEOPONT_KEY);
                                String location = e.getString(Keys.LOCATION_KEY);
                                String address = e.getString(Keys.ADDRESS_KEY);
                                boolean isOnline = e.getBoolean(Keys.ONLINE_KEY);
                                long reminderKey = e.getLong(Keys.REMINDER_KEY);

                                Log.d(TAG, "onComplete: Creating event id: " + id);
                                Event ev = new Event(name,
                                        owner,
                                        description,
                                        stime,
                                        etime,
                                        geoPoint.getLatitude(),
                                        geoPoint.getLongitude(),
                                        location,
                                        address,
                                        isOnline,
                                        reminderKey);
                                ev.addID(id);

                                if(owner.equals(userId)){
                                    eventList.add(ev);
                                } else {
                                    invitedList.add(ev);
                                }
                            }
                            initEvents();
                            initInvited();
                        }
                    }
                });

        FirebaseFirestore contacts = FirebaseFirestore.getInstance();
        contacts.collection(Keys.USER_KEY)
                .document(userId)
                .collection(Keys.FRIEND_KEY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot c : task.getResult()){
                                String uId = c.getId();
                                String firstname = c.getString(Keys.FIRSTNAME_KEY);
                                String surname = c.getString(Keys.SURNAME_KEY);
                                String email = c.getString(Keys.EMAIL_KEY);
                                String image = c.getString(Keys.IMAGE_KEY);

                                User co = new User(uId, firstname, surname, email, image);
                                contactList.add(co);
                            }
                            initContacts();
                        }
                    }
                });

    }

    private void initEvents(){
        Log.d(TAG, "initEvents: ");
        RecyclerView.Adapter eAdapter = new EventsAdapter(this, eventList);
        events.setAdapter(eAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        events.setLayoutManager(layoutManager);
    }
    
    private void initInvited(){
        Log.d(TAG, "initInvited: ");
        RecyclerView.Adapter iAdapter = new EventsAdapter(this, invitedList);
        invited.setAdapter(iAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        invited.setLayoutManager(layoutManager);
    }
    
    private void initContacts(){
        Log.d(TAG, "initContacts: ");
        RecyclerView.Adapter cAdapter = new UserAdapter(this, contactList);
        contacts.setAdapter(cAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        contacts.setLayoutManager(layoutManager);
    }
}
