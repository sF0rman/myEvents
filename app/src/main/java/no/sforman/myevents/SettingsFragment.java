package no.sforman.myevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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


    // User variables
    private String userId;
    private String firstname;
    private String surname;
    private String email;
    private String oldPassword;
    private String password;
    private String repeatPassword;
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
        getUserDetails();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
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

    }

    private void getUserDetails(){
        Log.d(TAG, "getUserDetails: Started");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        userId = currentUser.getUid();

        email = currentUser.getEmail();
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
                            
                            String f = document.getString("firstname");
                            String s = document.getString("surname");
                            String e = document.getString("email");
                            String img = document.getString("image");
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
        
        WarningDialogFragment warning = new WarningDialogFragment("Are you sure you want to delete all your events? This cannot be undone!",
                new WarningDialogFragment.WarningListener() {
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
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "onCompleted: User cancelled!");
                }
            }
        });

        warning.show(getFragmentManager(), "Warning");
        
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
        WarningDialogFragment warning = new WarningDialogFragment("Are you sure you want to delete you account? This will also remove all your events and cannot be undone", new WarningDialogFragment.WarningListener() {
            @Override
            public void onCompleted(boolean b) {
                if(b){
                    deleteAllEvents();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("user")
                            .document(userId)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "You account was deleted", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                    Intent deletedAccount = new Intent(getContext(), LoginActivity.class);
                                    startActivity(deletedAccount);
                                }
                            });

                }
            }
        });
    }

    public void acceptChange(){

        if(editUser){

        } else if (changePassword) {

        } else {

        }

    }

    public void cancelChange(){
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
    }

    private void getUserData(){

    }

    private void getEventData(){

    }

    private void getContactData(){

    }

}
