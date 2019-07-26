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

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Intent intent;
    public static final String TAG = "MainActivity";

    //UI
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView navView;
    private FrameLayout content;
    private View navHeader;

    private TextView username;
    private TextView email;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Firestore
    private FirebaseFirestore db;

    // Fragments
    EventsFragment eventsFragment;
    SettingsFragment settingsFragment;
    ContactFragment contactFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initUI();
        initNavigation();


        if(isOnline()){
            initFirebase();
            initUserData();
        } else {
            Log.e(TAG, "onCreate: No network");
        }

        // Don't reload fragment if device is rotated.
        if(intent.hasExtra("dir") && intent.getStringExtra("dir") == "contacts"){
            contactFragment = new ContactFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, contactFragment).commit();
            navView.setCheckedItem(R.id.nav_contacts);
            Log.d(TAG, "onCreate: intent-redirected to contacts.");
        } else if(intent.hasExtra("dir") && intent.getStringExtra("dir") == "settings"){
            settingsFragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, settingsFragment).commit();
            navView.setCheckedItem(R.id.nav_events);
            Log.d(TAG, "onCreate: intent-redirected to settings.");
        } else {
            eventsFragment = new EventsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, eventsFragment).commit();
            navView.setCheckedItem(R.id.nav_events);
            Log.d(TAG, "onCreate: Normal load to events page.");
        }

        onNavigationItemSelected(navView.getCheckedItem());

    }

    private void initUserData(){
        if(currentUser != null){
            username.setText(currentUser.getDisplayName());
            email.setText(currentUser.getEmail());
        } else {
            Log.e(TAG, "initUserData: No user logged on");
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
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
        navView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.main_toolbar);
        navHeader = navView.getHeaderView(0);

        username = navHeader.findViewById(R.id.nav_header_user_name);
        email = navHeader.findViewById(R.id.nav_header_email);
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
    }

    private void signOut(){
        mAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }


    @Override
    public void onBackPressed(){
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    // Navigation Listener
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_events:
                eventsFragment = new EventsFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, eventsFragment).commit();
                Log.d(TAG, "onNavigationItemSelected: Events loaded");
                break;
            case R.id.nav_contacts:
                contactFragment = new ContactFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, contactFragment).commit();
                Log.d(TAG, "onNavigationItemSelected: Contacts loaded");
                break;
            case R.id.nav_settings:
                settingsFragment = new SettingsFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, settingsFragment).commit();
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
