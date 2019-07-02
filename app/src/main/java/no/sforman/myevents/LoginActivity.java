package no.sforman.myevents;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    // UI
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginBtn;
    private Button forgotPasswordBtn;
    private Button createUserBtn;

    //FireBase
    private FirebaseAuth auth;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initUI();

        if(isOnline()){
            initFirebase();
        } else {
            Log.e(TAG, "onCreate: No network");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initUI(){
        emailInput = findViewById(R.id.login_input_email);
        passwordInput = findViewById(R.id.login_input_password);
        loginBtn = findViewById(R.id.login_btn_login);
        forgotPasswordBtn = findViewById(R.id.login_btn_forgot_password);
        createUserBtn = findViewById(R.id.login_btn_create_account);
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    private void initFirebase(){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }


    // Handle buttons

    public void onLogin(View v){

    }

    public void onCreateUser(View v){
        Intent i = new Intent(LoginActivity.this, CreateUserActivity.class);
        startActivity(i);
    }

    public void onForgotPassword(View v){

    }



}
