package no.sforman.myevents;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    public static final String TAG = "EventsAdapter";
    private Context mCtx;
    SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMMM yyyy @ HH:mm");

    private ArrayList<Event> events;

    public EventsAdapter(Context ctx, ArrayList<Event> events){
        this.mCtx = ctx;
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View eventCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_events, parent, false);
        EventViewHolder viewHolder = new EventViewHolder(eventCard);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, final int i) {
        Log.d(TAG, "onBindViewHolder: Started");

        holder.name.setText(events.get(i).getName());
        holder.location.setText(events.get(i).getLocation());
        holder.owner.setText("Hosted by: " + events.get(i).getOwner());
        holder.start.setText(dateFormat.format(events.get(i).getStart().getTime()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked ID" + events.get(i).getId());
            }
        });
    }

    @Override
    public int getItemCount() {
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
        private TextView owner;
        private CardView cardView;
        private ConstraintLayout layout;

        public EventViewHolder(@NonNull View itemView){
            super(itemView);

            this.name = itemView.findViewById(R.id.event_card_event_name);
            this.location = itemView.findViewById(R.id.event_card_event_location);
            this.owner = itemView.findViewById(R.id.event_card_event_owner);
            this.start = itemView.findViewById(R.id.event_card_event_start);
            this.cardView = itemView.findViewById(R.id.event_card_cardlayout);
            this.layout = itemView.findViewById(R.id.event_card_constraintlayout);
        }

    }



}
