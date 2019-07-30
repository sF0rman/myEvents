package no.sforman.myevents;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WarningDialogFragment extends androidx.fragment.app.DialogFragment {
    
    public static final String TAG = "WarningDialogFragment";

    String message;
    boolean password = false;

    View view;
    TextView messageField;
    Button acceptBtn;
    Button cancelBtn;
    EditText emailField;
    EditText passwordField;
    ProgressBar progressBar;

    WarningListener listener = null;

    public interface WarningListener {
        void onCompleted(boolean b);
    }

    public WarningDialogFragment(WarningListener l){
        this.listener = l;
        this.message = getString(R.string.msg_warning_default);
    }

    public WarningDialogFragment(String message, WarningListener l){
        this.message = message;
        this.listener = l;
    }

    public WarningDialogFragment(String message, boolean requiresPassword, WarningListener l){
        this.message = message;
        this.listener = l;
        this.password = requiresPassword;
    }

    public WarningDialogFragment(boolean requiresPassword, WarningListener l){
        this.message = getString(R.string.msg_warning_default);
        this.listener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        view = layoutInflater.inflate(R.layout.fragment_warning_dialog, null);

        builder.setView(view);

        messageField = view.findViewById(R.id.warning_message_field);
        acceptBtn = view.findViewById(R.id.warning_accept_button);
        cancelBtn = view.findViewById(R.id.warning_cancel_buttin);
        emailField = view.findViewById(R.id.warning_email);
        passwordField = view.findViewById(R.id.warning_password);
        progressBar = view.findViewById(R.id.warning_progress_bar);
        messageField.setText(message);


        if(password){
            emailField.setVisibility(View.VISIBLE);
            passwordField.setVisibility(View.VISIBLE);
        }

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                if(password){
                    Log.d(TAG, "onClick: Verifying password!");
                    String email = emailField.getText().toString();
                    String password = passwordField.getText().toString();
                    if(verifyInput(email, password)){
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(email, password);
                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.d(TAG, "onComplete: Authentication complete");
                                            listener.onCompleted(true);
                                            progressBar.setVisibility(View.GONE);
                                            dismiss();
                                        } else {
                                            Toast.makeText(getContext(), getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "onComplete: Incorrect username and password" + task.getException());
                                        }
                                    }
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }

                } else {
                    Log.d(TAG, "onClick: Accepted");
                    listener.onCompleted(true);
                    progressBar.setVisibility(View.GONE);
                    dismiss();
                }

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Cancelled");
                listener.onCompleted(false);
                dismiss();
            }
        });

        return builder.create();
    }

    public boolean verifyInput(String e, String p){
        Log.d(TAG, "verifyInput: Verifying input");

        boolean result = true;

        if(!isValidEmail(e)){
            result = false;
            Toast.makeText(getContext(), getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
        }

        if(p.length() < 1){
            result = false;
            Toast.makeText(getContext(), getString(R.string.error_invalid_password), Toast.LENGTH_SHORT).show();
        }

        return result;
    }

    private boolean isValidEmail(String e){
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return e.matches(regex);
    }
}
