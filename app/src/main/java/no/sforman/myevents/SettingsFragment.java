package no.sforman.myevents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

class SettingsFragment extends Fragment {

    public static final String TAG = "SettingsFragment";

    // UI
    private Button notificaitonSettings;
    private Button notificationChannelEvents;
    private Button notificationChannelInvites;
    private Button notificaitonChannelFriends;

    private Button userEdit;
    private Button userChangePassword;

    private Button privacyGetAllData;
    private Button privacyDeleteAllEvents;
    private Button privacyDeleteAccount;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }



}
