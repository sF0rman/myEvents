package no.sforman.myevents;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NoticeFragment extends Fragment {

    public static final String TAG = "NoticeFragment";
    View view;
    private String noticeText;
    private TextView noticeHolder;

    NoticeFragment() {
    } // Empty constructor

    NoticeFragment(String msg) {
        this.noticeText = msg;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.fragment_notice, container, false);
        noticeHolder = view.findViewById(R.id.notice_text);
        noticeText = getString(R.string.error_no_internet);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        noticeHolder.setText(noticeText);
    }
}
