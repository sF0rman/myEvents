package no.sforman.myevents;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

class ContactFragment extends Fragment {

    public static final String TAG = "ContactFragment";

    // UI
    private FloatingActionButton fab;
    private ConstraintLayout layout;
    private ProgressBar progressBar;

    private TextView subContacts;
    private TextView subRequests;

    // RecyclerView
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<User> userList = new ArrayList<>();

    // Firebase
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String userId;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (container == null) {
            return null;
        }

        layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_contact, container, false);

        progressBar = layout.findViewById(R.id.contacts_progress_bar);
        subContacts = layout.findViewById(R.id.contacts_sub_menu_contacts);
        subRequests = layout.findViewById(R.id.contacts_sub_menu_requests);


        initFab();
        initFire();

        return layout;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: OnStart");
        super.onStart();
        userList.clear();
        getContacts();
    }

    private void initFire() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "initFire: No user");
            Intent i = new Intent(getContext(), LoginActivity.class);
            startActivity(i);
        } else {
            userId = currentUser.getUid();
        }

    }

    private void initFab() {
        fab = layout.findViewById(R.id.contacts_add_contact_fab);
    }

    private void getContacts() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get all friends
        db.collection("user")
                .document(userId)
                .collection("friends")
                .orderBy("firstname")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "onComplete: Got contacts");

                            // For each person, add to RecyclerView
                            for (QueryDocumentSnapshot userDoc : task.getResult()) {
                                String id = userDoc.getId();
                                Log.d(TAG, "onComplete: Got document: " + id);
                                String firstname = userDoc.getString(Keys.FIRSTNAME_KEY);
                                String surname = userDoc.getString(Keys.SURNAME_KEY);
                                String email = userDoc.getString(Keys.EMAIL_KEY);
                                String image = userDoc.getString(Keys.IMAGE_KEY);

                                User u = new User(id,
                                        firstname,
                                        surname,
                                        email,
                                        image);

                                userList.add(u);
                            }

                            initRecyclerView();
                            progressBar.setVisibility(View.INVISIBLE);

                        } else {
                            Log.e(TAG, "onComplete: Couldn't get contacts", task.getException());
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });


    }


    private void initRecyclerView() {
        recyclerView = layout.findViewById(R.id.contacts_recycler_view);
        adapter = new UserAdapter(getContext(), userList);
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    public void getMyContacts() {
        subContacts.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        subContacts.setTextColor(ContextCompat.getColor(getContext(), R.color.lightPrimary));
        subRequests.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        subRequests.setTextColor(ContextCompat.getColor(getContext(), R.color.lightSecondary));
        getContacts();
    }

    public void getRequests() {
        progressBar.setVisibility(View.VISIBLE);

        subContacts.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        subContacts.setTextColor(ContextCompat.getColor(getContext(), R.color.lightSecondary));
        subRequests.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        subRequests.setTextColor(ContextCompat.getColor(getContext(), R.color.lightPrimary));

        Log.d(TAG, "getRequests: Started");

        userList.clear();

        

    }
}
