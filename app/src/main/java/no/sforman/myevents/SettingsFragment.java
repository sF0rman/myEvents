package no.sforman.myevents;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        view =  inflater.inflate(R.layout.fragment_settings, container, false);
        initUI();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserDetails();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initUI(){
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

    private void getUserDetails(){
        Log.d(TAG, "getUserDetails: Started");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        userId = currentUser.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            final DocumentReference docRef = db.collection("user").document(userId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()) {


                            Log.d(TAG, "onComplete: Got user");
                            
                            f = document.getString("firstname");
                            s = document.getString("surname");
                            e = document.getString("email");
                            img = document.getString("image");
                            firstnameInput.setText(f);
                            surnameInput.setText(s);
                            emailInput.setText(e);
                            Glide.with(getContext())
                                    .load(img)
                                    .into(profileImage);
                            profileName.setText(f + " " + s);
                            profileEmail.setText(e);
                        }
                    }
                }
            });
        } catch (NullPointerException e){
            Log.e(TAG, "getUserDetails: ", e);
        }
    }


    public void editUser(){
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

    public void changePassword(){
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

    public void getAllData(){
    }

    public void deleteAllEvents(){
        Log.d(TAG, "deleteAllEvents: Started");
        
        WarningDialogFragment warning = new WarningDialogFragment(getString(R.string.msg_warning_delete_all_events), new WarningDialogFragment.WarningListener() {
            @Override
            public void onCompleted(boolean b) {
                if(b){
                    Log.d(TAG, "onCompleted: User confirmed!");
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("event")
                            .whereEqualTo("owner", userId)
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "onComplete: Got events");
                                for(QueryDocumentSnapshot doc : task.getResult()){
                                    final String documentId = doc.getId();

                                    db.collection("event")
                                            .document(documentId)
                                            .collection("invited").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if(task.isSuccessful()){
                                                        Log.d(TAG, "onComplete: Got subcollection going");
                                                        for (QueryDocumentSnapshot goingDoc : task.getResult()){
                                                            String goingId = goingDoc.getId();
                                                            deleteSubDocs(documentId, goingId, "invited");
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
                } else {
                    userProgressbar.setVisibility(View.GONE);
                    Log.d(TAG, "onCompleted: User cancelled!");
                }
            }
        });

        warning.show(getFragmentManager(), "Warning");
        userProgressbar.setVisibility(View.VISIBLE);
        
    }

    private void deleteEvent(final String id){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("event")
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

    private void deleteSubDocs(final String docId, final String subDocid, final String subCol){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("event")
                .document(docId)
                .collection(subCol)
                .document(subDocid)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Deleted subDocument: " + subDocid);
                    }
                });
    }

    public void deleteAccount(){
        final WarningDialogFragment warning = new WarningDialogFragment(getString(R.string.msg_warning_delete_account), true,  new WarningDialogFragment.WarningListener() {
            @Override
            public void onCompleted(boolean b) {
                if(b){
                    deleteAllEvents();
                    deleteUser();
                }
            }
        });
        warning.show(getFragmentManager(), "Warning");
    }

    public void deleteUser(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user")
                .document(userId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        currentUser.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.d(TAG, "onComplete: User Accoutn deleted");
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

    public void acceptChange(){
        clearErrors();

        if(editUser){
            updateUserSettings();
        } else if (changePassword) {
            changeUserPassword();
        } else {
            Log.w(TAG, "acceptChange: Something went wrong");
            profileProgressbar.setVisibility(View.GONE);
        }

    }

    public void cancelChange(){
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

    private void updateUserSettings(){
        profileProgressbar.setVisibility(View.VISIBLE);
        final Context context = getContext();
        String newFirstname = firstnameInput.getText().toString();
        String newSurname = surnameInput.getText().toString();
        String newEmail = emailInput.getText().toString();

        if(verifyInput(newFirstname, newSurname, newEmail)){
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newFirstname + " " + newSurname)
                    .build();

            currentUser.updateProfile(profileUpdate)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "onComplete: Displayname updated successfully");
                            } else {
                                Toast.makeText(context, "Name change failed!", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onComplete: Displayname change error: ", task.getException());
                            }

                        }
                    });

            currentUser.updateEmail(newEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "onComplete: Email updated successfully");
                            } else {
                                Toast.makeText(context, "Email change failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            Map<Object, String> userData = new HashMap<>();
            userData.put(Keys.FIRSTNAME_KEY, newFirstname);
            userData.put(Keys.SURNAME_KEY, newSurname);
            userData.put(Keys.EMAIL_KEY, newEmail);
            userData.put(Keys.IMAGE_KEY, img);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("user")
                    .document(userId).set(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(context, getString(R.string.msg_success_user_data_change), Toast.LENGTH_SHORT).show();
                        cancelChange();
                        profileProgressbar.setVisibility(View.GONE);
                        getUserDetails();
                    } else {
                        Log.e(TAG, "onComplete: Couldn't write to firestore", task.getException());
                    }
                }
            });
        }
    }

    private void changeUserPassword(){
        profileProgressbar.setVisibility(View.VISIBLE);
        final Context context = getContext();
        final String oldPassword = oldPasswordInput.getText().toString();
        final String newPassword = passwordInput.getText().toString();
        final String newRepeatPassword = repeatPasswordInput.getText().toString();

        if(newPassword.length() > 4){
            if(newPassword.equals(newRepeatPassword)){
                Log.d(TAG, "changeUserPassword: Changing password...");

                AuthCredential credential = EmailAuthProvider
                        .getCredential(e, oldPassword);

                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    currentUser.updatePassword(newPassword)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
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

    private boolean verifyInput(String f, String s, String e){
        boolean inputOk = true;
        if(!isValidEmail(e)){
            inputOk = false;
            emailError.setText(R.string.error_invalid_email);
        }

        if(f.length() < 2){
            inputOk = false;
            firstnameError.setText(R.string.error_invalid_input);
        }

        if(s.length() < 2){
            surnameInput.setText(R.string.error_invalid_input);
        }

        return inputOk;
    }

    private void getEventData(){

    }

    private void getContactData(){

    }

    private boolean isValidEmail(String e){
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return e.matches(regex);
    }

    private void clearErrors(){
        firstnameError.setText("");
        surnameError.setText("");
        emailError.setText("");
        passwordError.setText("");
        repeatPasswordError.setText("");
    }

}
