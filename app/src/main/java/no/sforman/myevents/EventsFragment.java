package no.sforman.myevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.Date;

class EventsFragment extends Fragment {

    Intent intent;
    public static final String TAG = "EventFramgent";
    Calendar today = Calendar.getInstance();

    //UI
    private FloatingActionButton fab;
    private ConstraintLayout layout;

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

        initRecyclerView();
        initFab();
        initFire();
        getEvents();

        intent = getActivity().getIntent();


        return layout;
    }

    private void initFire(){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void getEvents(){
        db = FirebaseFirestore.getInstance();
        try {
            db.collection("event")
                    .whereEqualTo("owner", currentUser.getEmail())
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
                                    String name = document.get("name").toString();
                                    String owner = document.get("owner").toString();
                                    String description = document.get("description").toString();
                                    Calendar start = Calendar.getInstance();
                                    Calendar end = Calendar.getInstance();
                                    long stime = (long) document.get("start.timeInMillis");
                                    long etime = (long) document.get("end.timeInMillis");
                                    start.setTimeInMillis(stime);
                                    end.setTimeInMillis(etime);
                                    GeoPoint geoPoint = (GeoPoint) document.get("geoPoint");
                                    String location = document.get("location").toString();
                                    String address = document.get("address").toString();
                                    boolean isOnline = (boolean) document.get("online");
                                    long reminderKey = (long) document.get("reminderKey");

                                    Event e = new Event(name, owner, description, start, end,
                                            geoPoint.getLatitude(), geoPoint.getLongitude(),
                                            location, address, isOnline, reminderKey);
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
