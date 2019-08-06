package no.sforman.myevents;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateUserActivity extends AppCompatActivity {

    public static final String TAG = "CreateUserActivity";
    public static final int PICK_IMAGE = 1;

    // Connectivity
    private boolean connected = true;
    NoticeFragment noInternetWarning;


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
    private CircleImageView image;
    private ProgressBar progressBar;

    private Uri imageUri;

    // Firebase
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Lifecycle");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
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
        imageUri = null;
        image = findViewById(R.id.create_user_image);
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
        progressBar = findViewById(R.id.create_user_progress);
    }

    private void initFirebase() {
        Log.d(TAG, "initFirebase: ");
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        storageRef = mStorage.getReference();
    }

    public boolean isOnline() {
        Log.d(TAG, "isOnline: ");
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        connected = (networkInfo != null && networkInfo.isConnected());
        if (!connected) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.create_user_notice_container, noInternetWarning)
                    .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction().remove(noInternetWarning).commit();
        }
        return connected;
    }

    public void initUserData(FirebaseUser u) {
        Log.d(TAG, "initUserData: ");
        Intent i = new Intent(CreateUserActivity.this, MainActivity.class);
        progressBar.setVisibility(View.INVISIBLE);
        startActivity(i);
        finish();
    }

    private void clearErrors() {
        Log.d(TAG, "clearErrors: ");
        firstnameError.setText("");
        surnameError.setText("");
        emailError.setText("");
        passwordError.setText("");
        passwordRepeatError.setText("");
        termsError.setText("");
    }

    public void onCreateUser(View v) {
        Log.d(TAG, "onCreateUser: ");
        if (isOnline()) {
            progressBar.setVisibility(View.VISIBLE);
            clearErrors();

            Log.d(TAG, "onCreateUser: Started user creation");
            String f = firstname.getText().toString();
            String s = surname.getText().toString();
            String e = email.getText().toString();
            String pw = password.getText().toString();
            String pw2 = passwordRepeat.getText().toString();
            if (imageUri != null) {
                String img = imageUri.toString();
            }


            if (isValidInput(f, s, e, pw, pw2)) {
                Log.d(TAG, "onCreateUser: Input ok");
                createAccount(f, s, e, pw);
            } else {
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }

    }


    private boolean isValidInput(String firstname, String surname, String email, String password,
                                 String password2) {
        Log.d(TAG, "isValidInput: ");
        boolean isOk = true;

        if (firstname.length() < 2) {
            Log.e(TAG, "isValidInput: Firstname too short");
            firstnameError.setText(R.string.error_invalid_input);
            isOk = false;
        }
        if (surname.length() < 2) {
            Log.e(TAG, "isValidInput: Surname too short");
            surnameError.setText(R.string.error_invalid_input);
            isOk = false;
        }
        if (!isValidEmail(email)) {
            Log.e(TAG, "isValidInput: Email not valid");
            emailError.setText(R.string.error_invalid_email);
            isOk = false;
        }
        if (!terms.isChecked()) {
            Log.e(TAG, "isValidInput: Terms not agreed to");
            termsError.setText(R.string.error_terms_not_checked);
            isOk = false;
        }
        if (password.length() < 5) {
            Log.e(TAG, "isValidInput: Password too short");
            passwordError.setText(R.string.error_invalid_password);
            isOk = false;
        }
        if (!password.equals(password2)) {
            Log.e(TAG, "isValidInput: Passwords don't match---");
            passwordRepeatError.setText(R.string.error_password_dont_match);
            isOk = false;
        }

        return isOk;
    }

    private boolean isValidEmail(String e) {
        Log.d(TAG, "isValidEmail: ");
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return e.matches(regex);
    }

    private void createAccount(final String f, final String s, final String e, final String pw) {
        Log.d(TAG, "createAccount: ");
        mAuth.createUserWithEmailAndPassword(e, pw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            final String userId = user.getUid();

                            // Update displayname
                            UserProfileChangeRequest uChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName((f + " " + s).trim()).build();

                            user.updateProfile(uChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "DisplayNameAdded: success");

                                    if (imageUri != null) {
                                        // Upload profile image
                                        final StorageReference imgRef = storageRef.child("images/" + userId + ".jpg");
                                        imgRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            String downloadUrl = uri.toString();

                                                            // Update database data
                                                            populateDatabase(f, s, e, userId, downloadUrl);
                                                            // Send email
                                                            // sendEmail(user); // No active use for sendEmail. Not necessary

                                                            initUserData(user);

                                                        }
                                                    });
                                                } else {
                                                    Log.e(TAG, "onComplete: Unable to upload image", task.getException());
                                                    user.delete();
                                                    Log.d(TAG, "onComplete: Deleted user");
                                                }
                                            }
                                        });
                                    } else {
                                        // Update database data
                                        populateDatabase(f, s, e, userId, null);
                                        // Send email
                                        sendEmail(user);

                                        initUserData(user);
                                    }

                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            if (task.getException().toString().contains("The email address is already in use by another account.")) {
                                Toast.makeText(CreateUserActivity.this, "User with that email already exists!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CreateUserActivity.this, "An error occurred: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }

                            initUserData(null);
                        }
                    }
                });
    }


    private void sendEmail(FirebaseUser currentUser) {
        Log.d(TAG, "sendEmail: ");
        // send email verification
        currentUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createAccount: sendEmailVerification success.");
                        }
                    }
                });
    }

    private void populateDatabase(final String f, final String s, final String e, final String uId, final String imageUrl) {
        Log.d(TAG, "populateDatabase: ");
        // Add user to Firestore database (for use with friends/contacts)
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put(Keys.FIRSTNAME_KEY, f.trim());
        user.put(Keys.SURNAME_KEY, s.trim());
        user.put(Keys.EMAIL_KEY, e);
        user.put(Keys.IMAGE_KEY, imageUrl);


        db.collection("user").document(uId).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
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


    public void createSetImage(View v) {
        Log.d(TAG, "createSetImage: ");
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: ");
        if (requestCode == PICK_IMAGE) {
            try {
                imageUri = data.getData();
                image.setImageURI(imageUri);
                Log.d(TAG, "onActivityResult: ImageURI: " + imageUri.toString());
            } catch (RuntimeException e) {
                Log.e(TAG, "onActivityResult: No image was selected", e);
            }
        }
    }
}
