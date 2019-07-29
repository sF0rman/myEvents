package no.sforman.myevents;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ConfirmFragment extends Fragment implements DialogInterface.OnDismissListener, DialogInterface.OnCancelListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm, container, false);

        initUI();

        return view;
    }

    private void initUI(){
        
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {

    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {

    }
}
