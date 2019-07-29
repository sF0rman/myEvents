package no.sforman.myevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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


    // User variables
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
        return view;
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

    }

    private void displayConfirmBox(String msg, boolean requiresPassword){

    }

    private void getUserDetails(){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        email = currentUser.getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            final DocumentReference docRef = db.collection("user").document(email);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()) {
                            firstnameInput.setText(document.getString("firstname"));
                            surnameInput.setText(document.getString("surname"));
                            emailInput.setText(document.getString("email"));
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

    }

    public void deleteAccount(){

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
