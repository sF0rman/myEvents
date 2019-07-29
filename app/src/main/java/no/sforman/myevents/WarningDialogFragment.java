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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WarningDialogFragment extends androidx.fragment.app.DialogFragment {
    
    public static final String TAG = "WarningDialogFragment";

    String message;
    View view;
    TextView messageField;
    Button acceptBtn;
    Button cancelBtn;

    WarningListener listener = null;

    public interface WarningListener {
        void onCompleted(boolean b);
    }

    public WarningDialogFragment(String message, WarningListener l){
        this.message = message;
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

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Accepted");
                listener.onCompleted(true);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Cancelled");
                listener.onCompleted(false);
            }
        });

        return builder.create();
    }

}
