package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // Connectivity
    private boolean connected = true;
    private NoticeFragment noInternetWarning;

    // UI
    private EditText emailInput;
    private EditText passwordInput;
    private ProgressBar progressBar;

    //FireBase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Lifecycle");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        noInternetWarning = new NoticeFragment(getString(R.string.error_no_internet));
        initUI();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: Lifecycle");
        super.onStart();
        if (isOnline()) {
            initFirebase();
        } else {
            Log.e(TAG, "onCreate: No network");
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Lifecycle");
        super.onResume();
    }

    private void initUI() {
        Log.d(TAG, "initUI: ");
        progressBar = findViewById(R.id.login_progress);
        emailInput = findViewById(R.id.login_input_email);
        passwordInput = findViewById(R.id.login_input_password);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        super.onBackPressed();
        Intent exit = new Intent(Intent.ACTION_MAIN);
        exit.addCategory(Intent.CATEGORY_HOME);
        exit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(exit);
        finish();
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
                    .replace(R.id.login_notice_container, noInternetWarning)
                    .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction().remove(noInternetWarning).commit();
        }
        return connected;
    }

    private void initFirebase() {
        Log.d(TAG, "initFirebase: ");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        updateUserData(currentUser);
    }

    private void updateUserData(FirebaseUser user) {
        Log.d(TAG, "updateUserData: ");
        if (user != null) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    // Handle buttons

    public void onLogin(View v) {
        Log.d(TAG, "onLogin: ");
        if (isOnline()) {
            progressBar.setVisibility(View.VISIBLE);
            String e = emailInput.getText().toString();
            String p = passwordInput.getText().toString();
            if (isValidEmail(e) && inputPassword(p)) {
                login(e, p);
            } else {
                Toast.makeText(LoginActivity.this, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }


    private boolean isValidEmail(String e) {
        Log.d(TAG, "isValidEmail: ");
        String regex = "^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$";
        return e.matches(regex);
    }

    private boolean inputPassword(String p) {
        Log.d(TAG, "inputPassword: ");
        return p.length() > 0;
    }

    private void login(String email, String password) {
        Log.d(TAG, "login: ");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            progressBar.setVisibility(View.INVISIBLE);
                            updateUserData(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                    }
                });
    }

    public void onCreateUser(View v) {
        Log.d(TAG, "onCreateUser: ");
        Intent i = new Intent(LoginActivity.this, CreateUserActivity.class);
        startActivity(i);
    }

    public void onForgotPassword(View v) {
        Log.d(TAG, "onForgotPassword: ");
        if (isOnline()) {
            final WarningDialogFragment warning = new WarningDialogFragment(true, new WarningDialogFragment.WarningListener() {
                @Override
                public void onCompleted(boolean b) {
                    Log.d(TAG, "onCompleted: ");
                    // Do nothing
                }

                @Override
                public void onCompleted(boolean b, String email) {
                    Log.d(TAG, "onCompleted: " + b + "- email: " + email);
                    if(b){
                        updateUserData(null);
                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Email sent.");
                                        }
                                    }
                                });
                    } else {
                        Log.d(TAG, "onCompleted: nothing");
                    }
                }
            });
            warning.show(getSupportFragmentManager(), "ForgotPassword");
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }


    }


}
