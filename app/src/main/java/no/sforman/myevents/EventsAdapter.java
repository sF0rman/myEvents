package no.sforman.myevents;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private static final String TAG = "EventsAdapter";
    private Context mCtx;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMMM yyyy @ HH:mm", Locale.getDefault());

    private ArrayList<Event> events;

    public EventsAdapter(Context ctx, ArrayList<Event> events){
        this.mCtx = ctx;
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Lifecycle");
        View eventCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_events, parent, false);
        return new EventViewHolder(eventCard);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, final int i) {
        Log.d(TAG, "onBindViewHolder: Lifecycle");

        holder.name.setText(events.get(i).getName());
        if(events.get(i).getLocation().equals("none")){
            holder.location.setText(R.string.msg_event_online);
        } else {
            holder.location.setText(events.get(i).getLocation());
        }

        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(events.get(i).getStart());
        holder.start.setText(dateFormat.format(start.getTime()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked ID" + events.get(i).getId());
                openEvent(events.get(i).getId());
            }
        });
    }

    private void openEvent(String eventId){
        Log.d(TAG, "openEvent: ");
        Intent i = new Intent(mCtx, EventActivity.class);
        i.putExtra("eventId", eventId);
        mCtx.startActivity(i);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: ");
        if(events != null) {
            return events.size();
        } else {
            return 0;
        }
    }



    // ViewHolder Class

    public class EventViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView location;
        private TextView start;
        private CardView cardView;

        EventViewHolder(@NonNull View itemView){
            super(itemView);
            Log.d(TAG, "EventViewHolder: Lifecycle");

            this.name = itemView.findViewById(R.id.event_card_event_name);
            this.location = itemView.findViewById(R.id.event_card_event_location);
            this.start = itemView.findViewById(R.id.event_card_event_start);
            this.cardView = itemView.findViewById(R.id.event_card_card_view);
        }

    }



}
