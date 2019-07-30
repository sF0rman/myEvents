package no.sforman.myevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

class EventsFragment extends Fragment {

    public static final String TAG = "EventFramgent";
    Calendar today = Calendar.getInstance();

    //UI
    private FloatingActionButton fab;
    private ConstraintLayout layout;
    private ProgressBar progressBar;

    //RecyclerView
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Event> eventList = new ArrayList<Event>();

    // Firebase
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        if(container == null){
            return null;
        }

        layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_event, container, false);

        progressBar = layout.findViewById(R.id.event_fragment_progressbar);

        initFab();
        initFire();



        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        eventList.clear();
        getEvents();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    private void initFire(){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void getEvents(){
        progressBar.setVisibility(View.VISIBLE);
        db = FirebaseFirestore.getInstance();
        try {
            db.collection("event")
                    .whereEqualTo("owner", currentUser.getUid())
                    .orderBy("start", Query.Direction.ASCENDING)
                    .startAt(today)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "onComplete: Got events");
                                for(QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "onComplete: event" + document.toString());
                                    String id = document.getId();
                                    String name = document.getString("name");
                                    String owner = document.getString("owner");
                                    String description = document.getString("description");
                                    Calendar start = Calendar.getInstance();
                                    Calendar end = Calendar.getInstance();
                                    long stime = document.getLong("start.timeInMillis");
                                    long etime = document.getLong("end.timeInMillis");
                                    start.setTimeInMillis(stime);
                                    end.setTimeInMillis(etime);
                                    GeoPoint geoPoint = document.getGeoPoint("geoPoint");
                                    String location = document.getString("location");
                                    String address = document.getString("address");
                                    boolean isOnline = document.getBoolean("online");
                                    long reminderKey = document.getLong("reminderKey");

                                    Event e = new Event(name,
                                            owner,
                                            description,
                                            start,
                                            end,
                                            geoPoint.getLatitude(),
                                            geoPoint.getLongitude(),
                                            location,
                                            address,
                                            isOnline,
                                            reminderKey);

                                    e.addID(id);
                                    eventList.add(e);
                                }
                                Log.d(TAG, "Events" + eventList.toString());
                                initRecyclerView();
                            }
                        }
                    });
        } catch (NullPointerException e) {
            Log.e(TAG, "getEvents: Couldn't get events", e);
            Intent i = new Intent(getContext(), LoginActivity.class);
            startActivity(i);
        }

        progressBar.setVisibility(View.GONE);
    }

    private void initRecyclerView(){
        recyclerView = layout.findViewById(R.id.events_fragment_recycler_view);
        adapter = new EventsAdapter(getContext(), eventList);
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    private void initFab(){
        fab = layout.findViewById(R.id.event_add_event_fab);

    }


}
