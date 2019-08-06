package no.sforman.myevents;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

class SettingsFragment extends Fragment {

    public static final String TAG = "SettingsFragment";

    // UI
    View view;
    private Button userEdit;
    private Button userChangePassword;

    private Button userChangeAccept;
    private Button userChangeCancel;

    private EditText firstnameInput;
    private EditText surnameInput;
    private EditText emailInput;
    private EditText oldPasswordInput;
    private EditText passwordInput;
    private EditText repeatPasswordInput;

    private TextView firstnameError;
    private TextView surnameError;
    private TextView emailError;
    private TextView oldPasswordError;
    private TextView passwordError;
    private TextView repeatPasswordError;

    private CircleImageView profileImage;
    private TextView profileName;
    private TextView profileEmail;

    private ProgressBar profileProgressbar;
    private ProgressBar userProgressbar;


    // User variables
    private String userId;
    private String f;
    private String s;
    private String e;
    private String img;
    private boolean editUser = false;
    private boolean changePassword = false;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    SettingsListener settingsListener;

    public interface SettingsListener {
        public void onUserUpdated();
    }

    SettingsFragment(SettingsListener listener) {
        this.settingsListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: CreateView");
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: Started");
        super.onStart();
        initUI();
        initFirebase();

    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: Resumed");
        super.onResume();

        getUserDetails();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Intent noUser = new Intent(getContext(), LoginActivity.class);
            startActivity(noUser);
        }

    }

    private void initUI() {
        userEdit = view.findViewById(R.id.settings_edit_user);
        userChangePassword = view.findViewById(R.id.settings_change_password);
        userChangeAccept = view.findViewById(R.id.settings_user_accept);
        userChangeCancel = view.findViewById(R.id.settings_user_cancel);

        firstnameInput = view.findViewById(R.id.settings_firstname);
        surnameInput = view.findViewById(R.id.settings_surname);
        emailInput = view.findViewById(R.id.settings_email);
        oldPasswordInput = view.findViewById(R.id.settings_old_password);
        passwordInput = view.findViewById(R.id.settings_password);
        repeatPasswordInput = view.findViewById(R.id.settings_repeat_password);

        firstnameError = view.findViewById(R.id.settings_firstname_error);
        surnameError = view.findViewById(R.id.settings_surname_error);
        emailError = view.findViewById(R.id.settings_email_error);
        oldPasswordError = view.findViewById(R.id.settings_old_password_error);
        passwordError = view.findViewById(R.id.settings_password_error);
        repeatPasswordError = view.findViewById(R.id.settings_repeat_password_error);

        profileImage = view.findViewById(R.id.settings_profile_image);
        profileName = view.findViewById(R.id.settings_profile_name);
        profileEmail = view.findViewById(R.id.settings_profile_email);

        profileProgressbar = view.findViewById(R.id.settings_profile_progress_bar);
        userProgressbar = view.findViewById(R.id.settings_user_progress_bar);

    }

    private void getUserDetails() {
        Log.d(TAG, "getUserDetails: Started");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            final DocumentReference docRef = db.collection(Keys.USER_KEY).document(userId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {


                            Log.d(TAG, "onComplete: Got user");

                            f = document.getString(Keys.FIRSTNAME_KEY);
                            s = document.getString(Keys.SURNAME_KEY);
                            e = document.getString(Keys.EMAIL_KEY);
                            img = document.getString(Keys.IMAGE_KEY);
                            firstnameInput.setText(f);
                            surnameInput.setText(s);
                            emailInput.setText(e);
                            Glide.with(getContext())
                                    .load(img)
                                    .placeholder(R.drawable.ic_person)
                                    .into(profileImage);
                            profileName.setText(f + " " + s);
                            profileEmail.setText(e);
                        }
                    }
                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "getUserDetails: ", e);
        }
    }


    public void editUser() {
        clearErrors();
        editUser = true;
        firstnameInput.setVisibility(View.VISIBLE);
        surnameInput.setVisibility(View.VISIBLE);
        emailInput.setVisibility(View.VISIBLE);
        passwordInput.setVisibility(View.GONE);

        firstnameError.setVisibility(View.VISIBLE);
        surnameError.setVisibility(View.VISIBLE);
        emailError.setVisibility(View.VISIBLE);
        passwordError.setVisibility(View.VISIBLE);

        userChangePassword.setVisibility(View.GONE);
        userEdit.setVisibility(View.GONE);
        userChangeAccept.setVisibility(View.VISIBLE);
        userChangeCancel.setVisibility(View.VISIBLE);

        getUserDetails();
    }

    public void changePassword() {
        clearErrors();
        changePassword = true;
        oldPasswordInput.setVisibility(View.VISIBLE);
        passwordInput.setVisibility(View.VISIBLE);
        repeatPasswordInput.setVisibility(View.VISIBLE);

        oldPasswordError.setVisibility(View.VISIBLE);
        passwordError.setVisibility(View.VISIBLE);
        repeatPasswordError.setVisibility(View.VISIBLE);

        userChangePassword.setVisibility(View.GONE);
        userEdit.setVisibility(View.GONE);
        userChangeAccept.setVisibility(View.VISIBLE);
        userChangeCancel.setVisibility(View.VISIBLE);
    }

    public void getAllData() {
    }

    public void callDeleteAllEvents() {
        WarningDialogFragment warning = new WarningDialogFragment(getString(R.string.msg_warning_delete_all_events), new WarningDialogFragment.WarningListener() {
            @Override
            public void onCompleted(boolean b) {
                deleteAllEvents();
            }

            @Override
            public void onCompleted(boolean b, String email) {
                // Do nothing
            }
        });
        warning.show(getFragmentManager(), "WarningDeleteEvents");
    }

    public void deleteAllEvents() {
        Log.d(TAG, "deleteAllEvents: Started");

        Log.d(TAG, "onCompleted: User confirmed!");
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get every event that you have created!
        db.collection(Keys.EVENT_KEY)
                .whereEqualTo(Keys.OWNER_KEY, userId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Got events");
                    for (QueryDocumentSnapshot doc : task.getResult()) {

                        // Get documentId for event.
                        final String documentId = doc.getId();

                        final FirebaseFirestore docRef = FirebaseFirestore.getInstance();
                        // Get everyone invited to event
                        docRef.collection(Keys.EVENT_KEY)
                                .document(documentId)
                                .collection(Keys.INVITED_KEY)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "onComplete: Got subcollection going");
                                            // Remove everyone invited (regardless of rsvp)
                                            for (QueryDocumentSnapshot goingDoc : task.getResult()) {
                                                // Delete event from everyone who has the event.
                                                String goingId = goingDoc.getId();
                                                deleteSubDocs(Keys.EVENT_KEY, documentId, Keys.INVITED_KEY, goingId);
                                            }
                                        }
                                    }
                                });

                        final FirebaseFirestore subEvent = FirebaseFirestore.getInstance();
                        // Get all events under self
                        subEvent.collection(Keys.USER_KEY)
                                .document(userId)
                                .collection(Keys.EVENT_KEY)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {


                                            Log.d(TAG, "onComplete: Got all events under self");
                                            for (QueryDocumentSnapshot subEventDoc : task.getResult()) {
                                                String subEventId = subEventDoc.getId();
                                                // For each event delete it.
                                                deleteSubDocs(Keys.USER_KEY, userId, Keys.EVENT_KEY, subEventId);
                                                // For each event remove self from invited.
                                                deleteSubDocs(Keys.EVENT_KEY, subEventId, Keys.INVITED_KEY, userId);
                                            }
                                        }
                                    }
                                });

                        deleteEvent(documentId);

                    }
                } else {
                    Log.e(TAG, "onComplete: Couldn't get events", task.getException());
                    Toast.makeText(getContext(), "No events to delete!", Toast.LENGTH_SHORT).show();
                    userProgressbar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void deleteEvent(final String id) {
        FirebaseFirestore eventDb = FirebaseFirestore.getInstance();
        eventDb.collection(Keys.EVENT_KEY)
                .document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Deleted event: " + id);
                        userProgressbar.setVisibility(View.GONE);
                    }
                });
    }

    private void deleteSubDocs(final String col, final String docId, final String subCol, final String subDocId) {
        FirebaseFirestore subDb = FirebaseFirestore.getInstance();
        subDb.collection(col)
                .document(docId)
                .collection(subCol)
                .document(subDocId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Deleted subDocument: " + subDocId);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: Unable to delete subDocument", e);
            }
        });
    }

    public void deleteAccount() {
        final WarningDialogFragment warning = new WarningDialogFragment(getString(R.string.msg_warning_delete_account), true, new WarningDialogFragment.WarningListener() {
            @Override
            public void onCompleted(boolean b) {
                if (b) {
                    removeUserFromFriends();
                    deleteAllEvents();
                    deleteUser();
                }
            }

            @Override
            public void onCompleted(boolean b, String email) {
                // Do nothing
            }
        });
        warning.show(getFragmentManager(), "WarningDeleteAccount");
    }

    private void removeUserFromFriends() {
        FirebaseFirestore friendDb = FirebaseFirestore.getInstance();
        friendDb.collection(Keys.USER_KEY)
                .document(userId)
                .collection(Keys.FRIEND_KEY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Got all friends");
                            for (QueryDocumentSnapshot friend : task.getResult()) {
                                String friendId = friend.getId();
                                deleteSubDocs(Keys.USER_KEY, friendId, Keys.FRIEND_KEY, userId);
                            }
                        }
                    }
                });
    }

    public void deleteUser() {
        FirebaseFirestore userDb = FirebaseFirestore.getInstance();
        userDb.collection(Keys.USER_KEY)
                .document(userId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        currentUser.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "onComplete: User Account deleted");
                                            Toast.makeText(getContext(), "You account was deleted", Toast.LENGTH_SHORT).show();
                                            mAuth.signOut();
                                            Intent deletedAccount = new Intent(getContext(), LoginActivity.class);
                                            startActivity(deletedAccount);
                                        }
                                    }
                                });


                    }
                });
    }

    public void acceptChange() {
        clearErrors();

        if (editUser) {
            updateUserSettings();
        } else if (changePassword) {
            changeUserPassword();
        } else {
            Log.w(TAG, "acceptChange: Something went wrong");
            profileProgressbar.setVisibility(View.GONE);
        }

    }

    public void cancelChange() {
        clearErrors();
        editUser = false;
        changePassword = false;

        firstnameInput.setVisibility(View.GONE);
        surnameInput.setVisibility(View.GONE);
        emailInput.setVisibility(View.GONE);
        oldPasswordInput.setVisibility(View.GONE);
        passwordInput.setVisibility(View.GONE);
        repeatPasswordInput.setVisibility(View.GONE);

        firstnameError.setVisibility(View.GONE);
        surnameError.setVisibility(View.GONE);
        emailError.setVisibility(View.GONE);
        oldPasswordError.setVisibility(View.GONE);
        passwordError.setVisibility(View.GONE);
        repeatPasswordError.setVisibility(View.GONE);

        userChangePassword.setVisibility(View.VISIBLE);
        userEdit.setVisibility(View.VISIBLE);
        userChangeAccept.setVisibility(View.GONE);
        userChangeCancel.setVisibility(View.GONE);

        getUserDetails();
    }

    private void updateUserSettings() {
        profileProgressbar.setVisibility(View.VISIBLE);
        final Context context = getContext();
        String newFirstname = firstnameInput.getText().toString();
        String newSurname = surnameInput.getText().toString();
        String newEmail = emailInput.getText().toString();

        // Verify input
        if (verifyInput(newFirstname, newSurname, newEmail)) {
            Log.d(TAG, "updateUserSettings: InputVerified");
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newFirstname + " " + newSurname)
                    .build();

            // Update displaname in auth
            currentUser.updateProfile(profileUpdate)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Display name updated successfully");
                            } else {
                                Toast.makeText(context, "Name change failed!", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onComplete: Display name change error: ", task.getException());
                            }

                        }
                    });

            // Update email in auth
            currentUser.updateEmail(newEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Email updated successfully");
                            } else {
                                Toast.makeText(context, "Email change failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            // Create new map with userData
            final Map<Object, String> userData = new HashMap<>();
            userData.put(Keys.FIRSTNAME_KEY, newFirstname);
            userData.put(Keys.SURNAME_KEY, newSurname);
            userData.put(Keys.EMAIL_KEY, newEmail);
            userData.put(Keys.IMAGE_KEY, img);

            Log.d(TAG, "updateUserSettings: Map created");
            // Update details in user database
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(Keys.USER_KEY)
                    .document(userId).set(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "onComplete: Updating self");
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Changed userData in self");
                    } else {
                        Log.e(TAG, "onComplete: Couldn't write to firestore", task.getException());
                    }
                }
            });

            // Get all friends
            db.collection(Keys.USER_KEY)
                    .document(userId)
                    .collection(Keys.FRIEND_KEY)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d(TAG, "onComplete: updating friends");
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Got all friends");
                                for (QueryDocumentSnapshot friendDoc : task.getResult()) {
                                    // Edit user details in friend sub collection.
                                    String friendId = friendDoc.getId();
                                    Log.d(TAG, "onComplete: Updated for" + friendId);
                                    editSubDocs(Keys.USER_KEY, friendId, Keys.FRIEND_KEY, userId, userData);
                                }
                            }
                        }
                    });


            // Get all events where invited
            db.collection(Keys.USER_KEY)
                    .document(userId)
                    .collection(Keys.EVENT_KEY)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            Log.d(TAG, "onComplete: Got all events where invited");
                            for (QueryDocumentSnapshot eventDoc : queryDocumentSnapshots) {

                                // Edit user details in event subcollection
                                final String eventId = eventDoc.getId();
                                // Get rsvp reply so it doesn't change
                                FirebaseFirestore eventDb = FirebaseFirestore.getInstance();
                                eventDb.collection(Keys.EVENT_KEY)
                                        .document(eventId)
                                        .collection(Keys.INVITED_KEY)
                                        .document(userId)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot doc) {
                                                Log.d(TAG, "onComplete: Changing for event: " + eventId);
                                                if (doc.exists()) {
                                                    Log.d(TAG, "onComplete: Got RSVP reply");
                                                    String rsvp = doc.getString(Keys.RSVP_KEY);
                                                    userData.put(Keys.RSVP_KEY, rsvp);
                                                    editSubDocs(Keys.EVENT_KEY, eventId, Keys.INVITED_KEY, userId, userData);

                                                    // Display completed message and remove editboxes and progressbar
                                                    Toast.makeText(context, getString(R.string.msg_success_user_data_change), Toast.LENGTH_SHORT).show();
                                                    cancelChange();
                                                    getUserDetails();
                                                    settingsListener.onUserUpdated();
                                                    profileProgressbar.setVisibility(View.GONE);
                                                } else {
                                                    Log.d(TAG, "onSuccess: No documents");
                                                    profileProgressbar.setVisibility(View.GONE);
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "onFailure: ", e);
                                        profileProgressbar.setVisibility(View.GONE);
                                    }
                                });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: ", e);
                            profileProgressbar.setVisibility(View.GONE);
                        }
                    });

        }
    }


    private void editSubDocs(final String col, final String docId, final String subCol, final String subDocId, Map<Object, String> data) {
        FirebaseFirestore esdDb = FirebaseFirestore.getInstance();
        esdDb.collection(col)
                .document(docId)
                .collection(subCol)
                .document(subDocId)
                .set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Updated information in: " + col + "/" + docId + "/" + subCol + "/" + subDocId);
                } else {
                    Log.e(TAG, "onComplete: Could not update unformation", task.getException());
                }
            }
        });
    }

    private void changeUserPassword() {
        profileProgressbar.setVisibility(View.VISIBLE);
        final Context context = getContext();
        final String oldPassword = oldPasswordInput.getText().toString();
        final String newPassword = passwordInput.getText().toString();
        final String newRepeatPassword = repeatPasswordInput.getText().toString();

        if (newPassword.length() > 4) {
            if (newPassword.equals(newRepeatPassword)) {
                Log.d(TAG, "changeUserPassword: Changing password...");

                AuthCredential credential = EmailAuthProvider
                        .getCredential(e, oldPassword);

                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    currentUser.updatePassword(newPassword)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(context, getString(R.string.msg_success_new_password), Toast.LENGTH_SHORT).show();
                                                        cancelChange();
                                                        profileProgressbar.setVisibility(View.GONE);
                                                    } else {
                                                        Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                                        Log.w(TAG, "onComplete: Password change error: ", task.getException());
                                                        profileProgressbar.setVisibility(View.GONE);
                                                    }
                                                }
                                            });

                                } else {
                                    Toast.makeText(context, getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onComplete: Incorrect username and password" + task.getException());
                                    profileProgressbar.setVisibility(View.GONE);
                                }
                            }
                        });

            } else {
                repeatPasswordError.setText(R.string.error_password_dont_match);
                profileProgressbar.setVisibility(View.GONE);
            }
        } else {
            passwordError.setText(R.string.error_invalid_password);
            profileProgressbar.setVisibility(View.GONE);
        }
    }

    private boolean verifyInput(String f, String s, String e) {
        boolean inputOk = true;
        if (!isValidEmail(e)) {
            inputOk = false;
            emailError.setText(R.string.error_invalid_email);
        }

        if (f.length() < 2) {
            inputOk = false;
            firstnameError.setText(R.string.error_invalid_input);
        }

        if (s.length() < 2) {
            surnameInput.setText(R.string.error_invalid_input);
        }

        return inputOk;
    }

    private void getEventData() {

    }

    private void getContactData() {

    }

    private boolean isValidEmail(String e) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return e.matches(regex);
    }

    private void clearErrors() {
        firstnameError.setText("");
        surnameError.setText("");
        emailError.setText("");
        passwordError.setText("");
        repeatPasswordError.setText("");
    }

}
