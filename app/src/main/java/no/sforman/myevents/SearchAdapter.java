package no.sforman.myevents;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private static final String TAG = "SearchAdapter";

    private final Context sCtx;
    private final ArrayList<User> users;
    private ArrayList<User> selected;

    public SearchAdapter(Context context, ArrayList<User> users, ArrayList<User> selected, SelectionListener listener){
        this.sCtx = context;
        this.users = users;
        this.selected = selected;
        this.selectionListener = listener;
    }

    private SelectionListener selectionListener;
    public interface SelectionListener{
        void userSelected(String uId);
    }


    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Lifecycle");
        View searchCard = LayoutInflater.from(sCtx)
                .inflate(R.layout.card_search, parent, false);
        return new SearchViewHolder(searchCard);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int i) {
        Log.d(TAG, "onBindViewHolder: Lifecycle");
        final String userId = users.get(i).getId();
        final String name = users.get(i).getFullname();
        final String email = users.get(i).getEmail();
        final String image = users.get(i).getImage();

        for(User u : selected){
            if(users.get(i).getId().equals(u.getId())){
                // Hide users that already exist from search
                holder.card.setVisibility(View.GONE);
            }
        }

        Glide.with(sCtx)
                .load(image)
                .placeholder(R.drawable.ic_person)
                .into(holder.image);
        holder.name.setText(name);
        holder.email.setText(email);

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: selected user: " + userId);
                selectionListener.userSelected(userId);
            }
        });


    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: ");
        if(users != null){
            return users.size();
        } else {
            return 0;
        }
    }


    public class SearchViewHolder extends RecyclerView.ViewHolder {

        private final CardView card;
        private final CircleImageView image;
        private final TextView name;
        private final TextView email;

        SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "SearchViewHolder: Lifecycle");

            this.card = itemView.findViewById(R.id.search_card_card_view);
            this.image = itemView.findViewById(R.id.search_card_image);
            this.name = itemView.findViewById(R.id.search_card_name);
            this.email = itemView.findViewById(R.id.search_card_email);

        }
    }
}
