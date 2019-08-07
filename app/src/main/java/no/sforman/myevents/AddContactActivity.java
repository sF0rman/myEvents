package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddContactActivity extends AppCompatActivity implements SearchAdapter.SelectionListener, UserAdapter.ResponseListener {

    private static final String TAG = "AddContactActivity";

    private NoticeFragment noInternetWarning;

    // UI
    private EditText searchBar;
    private ProgressBar progressBar;
    private TextView nothingSelected;
    private RecyclerView selectedUsers;
    private RecyclerView searchResult;

    private FirebaseFirestore db;
    private String userId;

    // users
    private final ArrayList<User> allUsers = new ArrayList<>();
    private final ArrayList<User> userList = new ArrayList<>();
    private ArrayList<User> selectedUserList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Lifecycle");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        noInternetWarning = new NoticeFragment(getString(R.string.error_no_internet));

        initUI();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: Lifecycle");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Lifecycle");
        super.onResume();
        if (isOnline()) {
            initFire();
            getUsers();
        }
    }

    private void initUI() {
        Log.d(TAG, "initUI: ");
        searchBar = findViewById(R.id.add_contact_search_field);
        nothingSelected = findViewById(R.id.add_contact_nothing_selected);
        progressBar = findViewById(R.id.add_contact_progressbar);
        selectedUsers = findViewById(R.id.add_contact_selected);
        searchResult = findViewById(R.id.add_contact_search_result);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "beforeTextChanged: ");

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "onTextChanged: ");
                progressBar.setVisibility(View.VISIBLE);
                if (charSequence.toString().isEmpty()) {
                    userList.clear();
                    initSearchRecyclerView();
                } else {
                    searchUser(charSequence.toString());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "afterTextChanged: ");

            }
        });

    }

    private void initFire() {
        Log.d(TAG, "initFire: ");
        // Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (currentUser != null) {
            userId = currentUser.getUid();
            Log.d(TAG, "initFire: Logged in as user: " + userId);
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            Intent noUser = new Intent(AddContactActivity.this, LoginActivity.class);
            startActivity(noUser);
        }
    }

    private void getUsers() {
        Log.d(TAG, "getUsers: ");
        db.collection(Keys.USER_KEY).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Got all users");
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String id = doc.getId();
                        String firstname = doc.getString(Keys.FIRSTNAME_KEY);
                        String surname = doc.getString(Keys.SURNAME_KEY);
                        String email = doc.getString(Keys.EMAIL_KEY);
                        String image = doc.getString(Keys.IMAGE_KEY);

                        Log.d(TAG, "onComplete: Found: " + id);

                        User u = new User(id, firstname, surname, email, image);
                        allUsers.add(u);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    private void searchUser(String search) {
        Log.d(TAG, "searchUser: ");
        userList.clear();
        int limit = 10;
        int counter = 0;
        for (User u : allUsers) {
            if (u.getFullname().contains(search) || u.getEmail().contains(search)) {
                Log.d(TAG, "searchUser: Added user: " + u.getId());
                userList.add(u);
                initSearchRecyclerView();
                progressBar.setVisibility(View.INVISIBLE);
            }
            counter++;
            if (counter >= limit) {
                Log.d(TAG, "searchUser: Reached search limit");
                break;
            }
        }
    }

    private void hideNothingSelected() {
        Log.d(TAG, "hideNothingSelected: ");
        if (!selectedUserList.isEmpty()) {
            Log.d(TAG, "selectedUsers: isNotEmpty");
            nothingSelected.setVisibility(View.GONE);
            for (User u : selectedUserList) {
                Log.d(TAG, "initSelectedRecyclerView: Got: " + u.getId());
            }
        } else {
            Log.d(TAG, "selectedUsers: isEmpty");
            nothingSelected.setVisibility(View.VISIBLE);
        }
    }

    private void initSearchRecyclerView() {
        Log.d(TAG, "initSearchRecyclerView: ");
        hideNothingSelected();
        SearchAdapter searchAdapter = new SearchAdapter(this, userList, selectedUserList, this);
        searchResult.setAdapter(searchAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        searchResult.setLayoutManager(layoutManager);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void initSelectedRecyclerView() {
        Log.d(TAG, "initSelectedRecyclerView: ");
        hideNothingSelected();
        UserAdapter selectedAdapter = new UserAdapter(this, selectedUserList, "selected", this);
        selectedUsers.setAdapter(selectedAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        selectedUsers.setLayoutManager(layoutManager);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void userSelected(String uId) {
        Log.d(TAG, "userSelected: Returned user selection: " + uId);
        if (!uId.equals(userId)) {
            db.collection("user").document(uId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        String id = doc.getId();
                        String firstname = doc.getString(Keys.FIRSTNAME_KEY);
                        String surname = doc.getString(Keys.SURNAME_KEY);
                        String email = doc.getString(Keys.EMAIL_KEY);
                        String image = doc.getString(Keys.IMAGE_KEY);

                        Log.d(TAG, "onComplete: Selected: " + id);

                        User u = new User(id, firstname, surname, email, image);
                        selectedUserList.add(u);
                        searchBar.setText("");
                        initSelectedRecyclerView();
                        initSearchRecyclerView();
                    }
                }
            });
        } else {
            Toast.makeText(this, "You cannot add yourself as a contact", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void respondToRequest(String uId, boolean wasAccepted) {
        Log.d(TAG, "respondToRequest: ");
        // DO NOTHING HERE
    }

    @Override
    public void selectedUsers(ArrayList<User> users) {
        Log.d(TAG, "selectedUsers: ");
        selectedUserList = users;
        initSelectedRecyclerView();
    }

    private void addContacts() {
        Log.d(TAG, "addContacts: ");
        for (User u : selectedUserList) {
            final String id = u.getId();

            final Map<Object, String> userMap = new HashMap<>();
            userMap.put(Keys.SENDER_KEY, userId);
            userMap.put(Keys.RECIEVER_KEY, id);

            db.collection(Keys.REQUEST_KEY).add(userMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    Log.d(TAG, "onComplete: friendRequests sent");
                    progressBar.setVisibility(View.INVISIBLE);
                    done();
                }
            });
        }
    }

    public void accept(View v) {
        Log.d(TAG, "accept: ");
        if (isOnline()) {
            progressBar.setVisibility(View.VISIBLE);
            addContacts();
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    public void cancel(View v) {
        Log.d(TAG, "cancel: ");
        if (isOnline()) {
            done();
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void done() {
        Log.d(TAG, "done: ");
        Intent done = new Intent(AddContactActivity.this, MainActivity.class);
        progressBar.setVisibility(View.INVISIBLE);
        startActivity(done);
        finish();
    }

    private boolean isOnline() {
        Log.d(TAG, "isOnline: ");
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // Connectivity
        boolean connected = (networkInfo != null && networkInfo.isConnected());
        if (!connected) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.add_contact_notice_container, noInternetWarning)
                    .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction().remove(noInternetWarning).commit();
        }
        return connected;
    }
}
