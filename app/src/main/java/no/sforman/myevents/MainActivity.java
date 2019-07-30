package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Intent intent;
    public static final String TAG = "MainActivity";

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

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Firestore
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Fragments
    EventsFragment eventsFragment;
    SettingsFragment settingsFragment;
    ContactFragment contactFragment;
    NoticeFragment noticeFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initNavigation();


    }


    @Override
    protected void onStart() {
        super.onStart();

        // Don't reload fragment if device is rotated.
        if(intent.hasExtra("dir") && intent.getStringExtra("dir") == "contacts"){
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, new ContactFragment()).commit();
            toolbar.setTitle(R.string.title_contacts);
            navView.setCheckedItem(R.id.nav_contacts);
            Log.d(TAG, "onCreate: intent-redirected to contacts.");
        } else if(intent.hasExtra("dir") && intent.getStringExtra("dir") == "settings"){
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, new SettingsFragment()).commit();
            toolbar.setTitle(R.string.title_settings);
            navView.setCheckedItem(R.id.nav_events);
            Log.d(TAG, "onCreate: intent-redirected to settings.");
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, new EventsFragment()).commit();
            toolbar.setTitle(R.string.title_events);
            navView.setCheckedItem(R.id.nav_events);
            Log.d(TAG, "onCreate: Normal load to events page.");
        }

        onNavigationItemSelected(navView.getCheckedItem());

        if(isOnline()){
            initFirebase();
            initUserData();
        } else {
            Log.e(TAG, "onCreate: No network");
        }
    }

    private void initUserData(){
        if(currentUser != null){
            final String uId = currentUser.getUid();

            db = FirebaseFirestore.getInstance();
            final DocumentReference docRef = db.collection("user")
                    .document(uId);

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        final DocumentSnapshot document = task.getResult();
                        if(document.exists()) {
                            name.setText(document.getString("firstname") + " " + document.getString("surname"));
                            email.setText(document.getString("email"));
                            Log.d(TAG, "onComplete: ImageUri " + document.getString("image"));
                            try{
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference imgRef = storage.getReferenceFromUrl(document.getString("image"));
                                Glide.with(getApplicationContext())
                                        .load(document.getString("image"))
                                        .into(userImage);
                            } catch (Exception e){
                                Log.e(TAG, "onComplete: userImage: ", e);
                            }

                        }
                    }
                }
            });

        }
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    private void initUI(){
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

    private void initNavigation(){
        setSupportActionBar(toolbar);
        navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initFirebase(){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        if(currentUser == null) {
            Log.e(TAG, "initUserData: No user logged on");
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void signOut(){
        mAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }


    @Override
    public void onBackPressed(){
        if(drawer.isDrawerOpen(GravityCompat.START)){
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
        switch (menuItem.getItemId()){
            case R.id.nav_events:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, new EventsFragment()).commit();
                toolbar.setTitle(R.string.title_events);
                Log.d(TAG, "onNavigationItemSelected: Events loaded");
                break;
            case R.id.nav_contacts:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, new ContactFragment()).commit();
                toolbar.setTitle(R.string.title_contacts);
                Log.d(TAG, "onNavigationItemSelected: Contacts loaded");
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, new SettingsFragment()).commit();
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

    public void onAddEvent(View v){
        Intent i = new Intent(MainActivity.this, CreateEventActivity.class);
        startActivity(i);
    }

    public void notificationSettings(View v){
        Log.d(TAG, "notificationSettings: Opening notification settings...");
        Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        i.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
        startActivity(i);
    }

    public void testChannelEvent(View v){
        NotificationCompat.Builder eventBuild = new NotificationCompat.Builder(this, getString(R.string.channel_event))
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle(getString(R.string.msg_event))
                .setContentText(getString(R.string.msg_event_channel_working))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        sendNotification(eventBuild);
        Log.d(TAG, "testChannelEvent: testing event channel");
    }

    public void testChannelInvite(View v){
        NotificationCompat.Builder inviteBuild = new NotificationCompat.Builder(this, getString(R.string.channel_invite))
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle(getString(R.string.msg_invite))
                .setContentText(getString(R.string.msg_invite_channel_working))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        sendNotification(inviteBuild);
        Log.d(TAG, "testChannelInvite: testing invite channel");
    }

    public void testChannelFriends(View v){
        NotificationCompat.Builder friendBuild = new NotificationCompat.Builder(this, getString(R.string.channel_friend))
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle(getString(R.string.msg_friends))
                .setContentText(getString(R.string.msg_friend_channel_working))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        sendNotification(friendBuild);
        Log.d(TAG, "testChannelFriends: testing friend channel");
    }

    public void editUser(View v){
        settingsFragment.editUser();
    }

    public void changePassword(View v){
        settingsFragment.changePassword();
    }

    public void getAllData(View v){
        settingsFragment.getAllData();
    }

    public void deleteAllEvents(View v){
        settingsFragment.deleteAllEvents();
    }

    public void deleteAccount(View v){
        settingsFragment.deleteAccount();
    }

    public void acceptChange(View v){
        settingsFragment.acceptChange();
    }

    public void cancelChange(View v){
        settingsFragment.cancelChange();
    }

    public void sendNotification(NotificationCompat.Builder build){
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(0, build.build());
    }
}
