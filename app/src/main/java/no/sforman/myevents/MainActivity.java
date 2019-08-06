package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SettingsFragment.SettingsListener {

    private Intent intent;
    private static final String TAG = "MainActivity";

    // Connectivity
    private boolean connected = true;
    private NoticeFragment noInternetWarning;

    //UI
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView navView;
    private FrameLayout content;
    private FrameLayout notice;
    private View navHeader;

    private CircleImageView userImage;
    private TextView name;
    private TextView email;
    private String userImageUrl;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Firestore
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Fragments
    private EventsFragment eventsFragment;
    private SettingsFragment settingsFragment;
    private ContactFragment contactFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Lifecycle");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noInternetWarning = new NoticeFragment(getString(R.string.error_no_internet));

        initUI();
        initNavigation();

        contactFragment = new ContactFragment();
        settingsFragment = new SettingsFragment(this);
        eventsFragment = new EventsFragment();

    }


    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: Lifecycle");
        super.onStart();

        // Don't reload fragment if device is rotated.
        if (intent.hasExtra("dir") && intent.getStringExtra("dir").equals("contacts")) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, contactFragment).commit();
            toolbar.setTitle(R.string.title_contacts);
            navView.setCheckedItem(R.id.nav_contacts);
            Log.d(TAG, "onCreate: intent-redirected to contacts.");
        } else if (intent.hasExtra("dir") && intent.getStringExtra("dir").equals("settings")) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, settingsFragment).commit();
            toolbar.setTitle(R.string.title_settings);
            navView.setCheckedItem(R.id.nav_events);
            Log.d(TAG, "onCreate: intent-redirected to settings.");
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, eventsFragment).commit();
            toolbar.setTitle(R.string.title_events);
            navView.setCheckedItem(R.id.nav_events);
            Log.d(TAG, "onCreate: Normal load to events page.");
        }

        onNavigationItemSelected(navView.getCheckedItem());

    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Lifecycle");
        super.onResume();
        noInternetWarning = new NoticeFragment(getString(R.string.error_no_internet));

        Log.d(TAG, "onResume: CheckingNetWork and connecting");
        if (isOnline()) {
            initFirebase();
            initUserData();
        } else {
            Log.e(TAG, "onCreate: No network");
        }
    }

    private void initUserData() {
        Log.d(TAG, "initUserData: ");
        if (currentUser != null) {
            final String uId = currentUser.getUid();

            db = FirebaseFirestore.getInstance();
            final DocumentReference docRef = db.collection("user")
                    .document(uId);

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        final DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            name.setText(document.getString("firstname") + " " + document.getString("surname"));
                            email.setText(document.getString("email"));
                            userImageUrl = document.getString("image");
                            Log.d(TAG, "onComplete: ImageUri " + document.getString("image"));
                            try {
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference imgRef = storage.getReferenceFromUrl(document.getString("image"));
                                if (imgRef != null) {
                                    Glide.with(getApplicationContext())
                                            .load(userImageUrl)
                                            .placeholder(R.drawable.ic_person)
                                            .into(userImage);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "onComplete: userImage: ", e);
                            }

                        }
                    }
                }
            });

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
                    .replace(R.id.main_notice_container, noInternetWarning)
                    .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction().remove(noInternetWarning).commit();
        }
        return connected;
    }

    private void initUI() {
        Log.d(TAG, "initUI: ");
        intent = getIntent();

        drawer = findViewById(R.id.main_drawer);
        content = findViewById(R.id.main_content_container);
        notice = findViewById(R.id.main_notice_container);
        navView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.main_toolbar);
        navHeader = navView.getHeaderView(0);

        name = navHeader.findViewById(R.id.nav_header_user_name);
        email = navHeader.findViewById(R.id.nav_header_email);
        userImage = navHeader.findViewById(R.id.nav_header_image);
    }

    private void initNavigation() {
        Log.d(TAG, "initNavigation: ");
        setSupportActionBar(toolbar);
        navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initFirebase() {
        Log.d(TAG, "initFirebase: ");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        if (currentUser == null) {
            Log.e(TAG, "initUserData: No user logged on");
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void signOut() {
        Log.d(TAG, "signOut: ");
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }


    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent exit = new Intent(Intent.ACTION_MAIN);
            exit.addCategory(Intent.CATEGORY_HOME);
            exit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(exit);
            finish();
        }
    }

    // Navigation Listener
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.d(TAG, "onNavigationItemSelected: ");
        isOnline();
        switch (menuItem.getItemId()) {
            case R.id.nav_events:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, eventsFragment).commit();
                toolbar.setTitle(R.string.title_events);
                Log.d(TAG, "onNavigationItemSelected: Events loaded");
                break;
            case R.id.nav_contacts:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, contactFragment).commit();
                toolbar.setTitle(R.string.title_contacts);
                Log.d(TAG, "onNavigationItemSelected: Contacts loaded");
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, settingsFragment).commit();
                toolbar.setTitle(R.string.title_settings);
                Log.d(TAG, "onNavigationItemSelected: Settings loaded");
                break;
            case R.id.nav_logout:
                signOut();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onAddEvent(View v) {
        Log.d(TAG, "onAddEvent: ");
        Intent i = new Intent(MainActivity.this, CreateEventActivity.class);
        startActivity(i);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void notificationSettings(View v) {
        Log.d(TAG, "notificationSettings: ");
        Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        i.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
        startActivity(i);
    }

    public void testChannelEvent(View v) {
        Log.d(TAG, "testChannelEvent: ");
        NotificationCompat.Builder eventBuild = new NotificationCompat.Builder(this, getString(R.string.channel_event))
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle(getString(R.string.msg_event))
                .setContentText(getString(R.string.msg_event_channel_working))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        sendNotification(eventBuild);
        Log.d(TAG, "testChannelEvent: testing event channel");
    }

    public void testChannelInvite(View v) {
        Log.d(TAG, "testChannelInvite: ");
        NotificationCompat.Builder inviteBuild = new NotificationCompat.Builder(this, getString(R.string.channel_invite))
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle(getString(R.string.msg_invite))
                .setContentText(getString(R.string.msg_invite_channel_working))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        sendNotification(inviteBuild);
        Log.d(TAG, "testChannelInvite: testing invite channel");
    }

    public void testChannelFriends(View v) {
        Log.d(TAG, "testChannelFriends: ");
        NotificationCompat.Builder friendBuild = new NotificationCompat.Builder(this, getString(R.string.channel_friend))
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle(getString(R.string.msg_friends))
                .setContentText(getString(R.string.msg_friend_channel_working))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        sendNotification(friendBuild);
        Log.d(TAG, "testChannelFriends: testing friend channel");
    }

    private void sendNotification(NotificationCompat.Builder build) {
        Log.d(TAG, "sendNotification: ");
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(0, build.build());
    }

    public void mEditUser(View v) {
        Log.d(TAG, "mEditUser: ");
        settingsFragment.editUser();
    }

    public void mChangePassword(View v) {
        Log.d(TAG, "mChangePassword: ");
        settingsFragment.changePassword();
    }

    public void mGetAllData(View v) {
        Log.d(TAG, "mGetAllData: ");
        if (isOnline()) {
            settingsFragment.getAllData();
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    public void mDeleteAllEvents(View v) {
        Log.d(TAG, "mDeleteAllEvents: ");
        if (isOnline()) {
            settingsFragment.callDeleteAllEvents();
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    public void mDeleteAccount(View v) {
        Log.d(TAG, "mDeleteAccount: ");
        if (isOnline()) {
            settingsFragment.deleteAccount();
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    public void mAcceptChange(View v) {
        Log.d(TAG, "mAcceptChange: ");
        if (isOnline()) {
            settingsFragment.acceptChange();
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }

    }

    public void mCancelChange(View v) {
        Log.d(TAG, "mCancelChange: ");
        settingsFragment.cancelChange();
    }

    public void mGetAllEvents(View v) {
        Log.d(TAG, "mGetAllEvents: ");
        if (isOnline()) {
            eventsFragment.getAllEvents();
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    public void mGetMyEvents(View v) {
        Log.d(TAG, "mGetMyEvents: ");
        if (isOnline()) {
            eventsFragment.getMyEvents();
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    public void mGetContacts(View v) {
        Log.d(TAG, "mGetContacts: ");
        if (isOnline()) {
            contactFragment.getMyContacts();
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    public void mGetRequests(View v) {
        Log.d(TAG, "mGetRequests: ");
        if (isOnline()) {
            contactFragment.getMyRequests();
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    public void mAddContact(View v) {
        Log.d(TAG, "mAddContact: ");
        contactFragment.addContact();
    }

    @Override
    public void onUserUpdated() {
        Log.d(TAG, "onUserUpdated: ");
        initUserData();
    }
}
