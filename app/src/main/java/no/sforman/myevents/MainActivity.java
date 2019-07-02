package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, new ContactFragment());
            navView.setCheckedItem(R.id.nav_contacts);
            Log.d(TAG, "onCreate: intent-redirected to contacts.");
        } else if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, new EventsFragment());
            navView.setCheckedItem(R.id.nav_events);
            Log.d(TAG, "onCreate: Normal load to events page.");
        }

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



    // Navigation Listener
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_events:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, new EventsFragment()).commit();
                break;
            case R.id.nav_contacts:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, new ContactFragment()).commit();
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content_container, new SettingsFragment()).commit();
                break;
            case R.id.nav_logout:
                signOut();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
