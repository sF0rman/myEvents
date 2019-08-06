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

    private static final String TAG = "NoticeFragment";
    private View view;
    private String noticeText;
    private TextView noticeHolder;

    NoticeFragment(String msg) {
        this.noticeText = msg;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Lifecycle");
        view = inflater.inflate(R.layout.fragment_notice, container, false);
        noticeHolder = view.findViewById(R.id.notice_text);
        noticeText = getString(R.string.error_no_internet);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Lifecycle");
        noticeHolder.setText(noticeText);
    }
}
