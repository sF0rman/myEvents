package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    public static final String TAG = "AddContactActivity";

    // UI
    private EditText searchBar;
    private ProgressBar progressBar;
    private TextView nothingSelected;
    private RecyclerView selectedUsers;
    private RecyclerView searchResult;
    private Button cancelBtn;
    private Button acceptBtn;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private String userId;
    Map<Object, String> self;
    SearchAdapter searchAdapter;
    UserAdapter selectedAdapter;

    // users
    private ArrayList<User> allUsers = new ArrayList<>();
    private ArrayList<User> userList = new ArrayList<>();
    private ArrayList<User> selectedUserList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        initUI();
        initFire();
    }

    private void initUI() {
        searchBar = findViewById(R.id.add_contact_search_field);
        nothingSelected = findViewById(R.id.add_contact_nothing_selected);
        progressBar = findViewById(R.id.add_contact_progressbar);
        selectedUsers = findViewById(R.id.add_contact_selected);
        searchResult = findViewById(R.id.add_contact_search_result);
        acceptBtn = findViewById(R.id.add_contact_accept);
        cancelBtn = findViewById(R.id.add_contact_cancel);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                progressBar.setVisibility(View.VISIBLE);
                initSearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void initFire() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
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

    private void getUsers(){
        db.collection(Keys.USER_KEY).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: Got all users");
                    for(QueryDocumentSnapshot doc : task.getResult()){
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

    private void searchUser(String search){
        userList.clear();
        int limit = 20;
        int counter = 0;
        for(User u : allUsers){
            if(u.getFullname().contains(search) || u.getEmail().contains(search)){
                Log.d(TAG, "searchUser: Added user: " + u.getId());
                userList.add(u);
            }
            counter++;
            if(limit > 20){
                Log.d(TAG, "searchUser: Reached search limit");
                break;
            }
        }
    }


    private void initSearch(String search) {
        userList.clear();

        Query query = db.collection(Keys.USER_KEY)
                .orderBy(Keys.FIRSTNAME_KEY)
                .startAt(search)
                .endAt(search)
                .limit(20);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Got results");
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String id = doc.getId();
                        String firstname = doc.getString(Keys.FIRSTNAME_KEY);
                        String surname = doc.getString(Keys.SURNAME_KEY);
                        String email = doc.getString(Keys.EMAIL_KEY);
                        String image = doc.getString(Keys.IMAGE_KEY);

                        Log.d(TAG, "onComplete: Found: " + id);

                        User u = new User(id, firstname, surname, email, image);
                        userList.add(u);
                    }

                    progressBar.setVisibility(View.INVISIBLE);
                    initSearchRecyclerView();

                }
            }
        });
    }


    private void initSearchRecyclerView() {
        searchAdapter = new SearchAdapter(this, userList, selectedUserList, this);
        searchResult.setAdapter(searchAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        searchResult.setLayoutManager(layoutManager);
    }

    private void initSelectedRecyclerView() {
        if(!selectedUserList.isEmpty()){
            nothingSelected.setVisibility(View.GONE);
            for(User u : selectedUserList){
                Log.d(TAG, "initSelectedRecyclerView: Got: " + u.getId());
            }
        } else {
            nothingSelected.setVisibility(View.VISIBLE);
        }
        selectedAdapter = new UserAdapter(this, selectedUserList, "selected", this);
        selectedUsers.setAdapter(selectedAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        selectedUsers.setLayoutManager(layoutManager);
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

                        Log.d(TAG, "onComplete: Found" + id);

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
        // DO NOTHING HERE
    }

    @Override
    public void selectedUsers(ArrayList<User> users) {
        selectedUserList = users;
        initSelectedRecyclerView();
    }

    private void addContacts() {
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
        progressBar.setVisibility(View.VISIBLE);
        addContacts();
    }

    public void cancel(View v) {
        done();
    }

    private void done() {
        Intent done = new Intent(AddContactActivity.this, MainActivity.class);
        progressBar.setVisibility(View.INVISIBLE);
        startActivity(done);
        finish();
    }
}
