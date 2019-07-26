package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateUserActivity extends AppCompatActivity {

    public static final String TAG = "CreateUserActivity";

    // UI
    private EditText firstname;
    private EditText surname;
    private EditText email;
    private EditText password;
    private EditText passwordRepeat;
    private CheckBox terms;
    private TextView firstnameError;
    private TextView surnameError;
    private TextView emailError;
    private TextView passwordError;
    private TextView passwordRepeatError;
    private TextView termsError;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        initUI();

        if(isOnline()){
            initFirebase();
        } else {
            Log.e(TAG, "onCreate: No network");
        }
    }

    private void initUI(){
        firstname = findViewById(R.id.create_user_firstname);
        surname = findViewById(R.id.create_user_surname);
        email = findViewById(R.id.create_user_email);
        password = findViewById(R.id.create_user_password);
        passwordRepeat = findViewById(R.id.create_user_repeat_password);
        terms = findViewById(R.id.create_user_terms_checkbox);
        firstnameError = findViewById(R.id.create_user_firstname_error);
        surnameError = findViewById(R.id.create_user_surname_error);
        emailError = findViewById(R.id.create_user_email_error);
        passwordError = findViewById(R.id.create_user_password_error);
        passwordRepeatError = findViewById(R.id.create_user_repeat_password_error);
        termsError = findViewById(R.id.create_user_terms_error);
    }

    private void initFirebase(){
        mAuth = FirebaseAuth.getInstance();
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    public void initUserData(FirebaseUser u){
        if(u != null){
            Intent i = new Intent(CreateUserActivity.this, MainActivity.class);
            startActivity(i);
        }
    }

    private void clearErrors(){
        firstnameError.setText("");
        surnameError.setText("");
        emailError.setText("");
        passwordError.setText("");
        passwordRepeatError.setText("");
        termsError.setText("");
    }

    public void onCreateUser(View v){
        clearErrors();

        Log.d(TAG, "onCreateUser: Started user creation");
        String f = firstname.getText().toString();
        String s = surname.getText().toString();
        String e = email.getText().toString();
        String pw = password.getText().toString();
        String pw2 = passwordRepeat.getText().toString();

        if(isValidInput(f, s, e, pw, pw2)){
            Log.d(TAG, "onCreateUser: Input ok");
            createAccount(f, s, e, pw);
        }
    }


    private boolean isValidInput(String firstname, String surname, String email, String password,
                                 String password2){
        boolean isOk = true;

        if(firstname.length() < 2){
            Log.e(TAG, "isValidInput: Firstname too short");
            firstnameError.setText(R.string.error_invalid_input);
            isOk = false;
        }
        if(surname.length() < 2){
            Log.e(TAG, "isValidInput: Surname too short");
            surnameError.setText(R.string.error_invalid_input);
            isOk = false;
        }
        if(!isValidEmail(email)){
            Log.e(TAG, "isValidInput: Email not valid");
            emailError.setText(R.string.error_invalid_email);
            isOk = false;
        }
        if(!terms.isChecked()){
            Log.e(TAG, "isValidInput: Terms not agreed to");
            termsError.setText(R.string.error_terms_not_checked);
            isOk = false;
        }
        if(password.length() < 5){
            Log.e(TAG, "isValidInput: Password too short");
            passwordError.setText(R.string.error_invalid_password);
            isOk = false;
        }
        if(!password.equals(password2)){
            Log.e(TAG, "isValidInput: Passwords don't match---");
            passwordRepeatError.setText(R.string.error_password_dont_match);
            isOk = false;
        }

        return isOk;
    }

    private boolean isValidEmail(String e){
            String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
            return e.matches(regex);
    }

    private void createAccount(final String f, final String s, final String e, final String pw){
        mAuth.createUserWithEmailAndPassword(e, pw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            updateUserName(user, f + " " + s);
                            sendEmail(user);
                            populateDatabase(user, f, s, e);

                            initUserData(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            initUserData(null);
                        }
                    }
                });
    }

    private void updateUserName(FirebaseUser currentUser, String n){
        // Add Displayname to user.
        UserProfileChangeRequest uChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(n.trim()).build();

        currentUser.updateProfile(uChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "DisplayNameAdded: success");
            }
        });
    }

    private void sendEmail(FirebaseUser currentUser){
        // send email verification
        currentUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "createAccount: sendEmailVerification success.");
                        }
                    }
                });
    }

    private void populateDatabase(final FirebaseUser currentUser, final String f,
                                  final String s, final String e){
        // Add user to Firestore database (for use with friends/contacts)
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put(Keys.FIRSTNAME_KEY, f.trim());
        user.put(Keys.SURNAME_KEY, s.trim());
        user.put(Keys.EMAIL_KEY, e);

        db.collection("user").document(e).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "AddedUserToFireStore:success");
                Toast.makeText(CreateUserActivity.this, "User created!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "AddedUserToFireStore:failure", e);
                Toast.makeText(CreateUserActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();
            }
        });
    }
}
