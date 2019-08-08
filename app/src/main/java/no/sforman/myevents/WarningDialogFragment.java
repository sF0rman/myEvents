package no.sforman.myevents;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

class WarningDialogFragment extends androidx.fragment.app.DialogFragment {
    
    private static final String TAG = "WarningDialogFragment";

    private String message;
    private boolean password = false;
    private boolean emailOnly = false;

    private View view;
    private TextView messageField;
    private Button acceptBtn;
    private Button cancelBtn;
    private EditText emailField;
    private EditText passwordField;
    private ProgressBar progressBar;

    private WarningListener listener = null;

    public interface WarningListener {
        void onCompleted(boolean b);
        void onCompleted(boolean b, String email);
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

    public WarningDialogFragment(boolean emailOnly, WarningListener l){
        this.message = "Input your email!";
        this.emailOnly = emailOnly;
        this.listener = l;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Lifecycle");

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
        } else if (emailOnly) {
            emailField.setVisibility(View.VISIBLE);
        }

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                if(password){
                    Log.d(TAG, "onClick: Verifying password!");
                    final String email = emailField.getText().toString().trim();
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
                    }

                } else if (emailOnly) {
                    Log.d(TAG, "onClick: Email required");
                    final String email = emailField.getText().toString();
                    if(!email.isEmpty()){
                        Log.d(TAG, "onClick: Email: " + email);
                        if(isValidEmail(email)){
                            // Default password
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, "mev")
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if(!task.isSuccessful()){
                                                try{
                                                    throw task.getException();
                                                } catch (FirebaseAuthInvalidUserException invalidEmail){
                                                    Toast.makeText(getContext(), R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
                                                } catch (FirebaseAuthInvalidCredentialsException invalidPassword){
                                                    listener.onCompleted(true, email);
                                                    Toast.makeText(getContext(), getString(R.string.msg_email_sent) + " " + email, Toast.LENGTH_SHORT).show();
                                                    dismiss();
                                                } catch (Exception e){
                                                    Log.e(TAG, "onComplete: ", e);
                                                    Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(getContext(), R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }


                    } else {
                        Toast.makeText(getContext(), R.string.error_invalid_input, Toast.LENGTH_SHORT).show();
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

    private boolean verifyInput(String e, String p){
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
        Log.d(TAG, "isValidEmail: ");
        String regex = "^[\\w-_.+]*[\\w-_.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return e.matches(regex);
    }
}
