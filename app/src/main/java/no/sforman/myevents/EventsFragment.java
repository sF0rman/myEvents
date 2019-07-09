package no.sforman.myevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

class EventsFragment extends Fragment {

    //UI
    private FloatingActionButton fab;
    private ConstraintLayout layout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        if(container == null){
            return null;
        }

        layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_event, container, false);
        initFab();
        getEvents();

        return layout;
    }

    private void getEvents(){

    }

    private void initFab(){
        fab = layout.findViewById(R.id.event_add_event_fab);

    }
}
