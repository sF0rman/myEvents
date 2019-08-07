package no.sforman.myevents;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private static final String TAG = "UserAdapter";

    private Context uCtx;
    private String type = "default";
    private String requestId;

    private ArrayList<User> users;
    private ArrayList<User> selectedUsers = new ArrayList<>();

    /**
     * @param context <Context>
     * @param users   <ArrayList of User objects>
     */
    public UserAdapter(Context context, ArrayList<User> users) {
        this.uCtx = context;
        this.users = users;
    }


    /**
     * @param context  <Context>
     * @param users    <ArrayList of User objects>
     * @param type     <Type can be request, add or selected. request displays accept/decline buttons, add toggles adding/removing users. selected removes users from list.>
     * @param listener <RemoveSelectionListener is required if type is defined.>
     */
    public UserAdapter(Context context, ArrayList<User> users, String type, ResponseListener listener) {
        this.uCtx = context;
        this.users = users;
        this.type = type;
        this.onClickListener = listener;
    }

    /**
     * @param context  <Context>
     * @param users    <ArrayList of User objects>
     * @param type     <Type can be request, add or selected. request displays accept/decline buttons, add toggles adding/removing users. selected removes users from list.>
     * @param requestId <requestId from database>
     * @param listener <RemoveSelectionListener is required if type is defined.>
     */
    public UserAdapter(Context context, ArrayList<User> users, String type, String requestId, ResponseListener listener) {
        this.uCtx = context;
        this.users = users;
        this.type = type;
        this.requestId = requestId;
        this.onClickListener = listener;
    }

    private ResponseListener onClickListener;

    public interface ResponseListener {
        void respondToRequest(String requestId, boolean wasAccepted);
        void selectedUsers(ArrayList<User> users);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Lifecycle");
        View userCard = LayoutInflater.from(uCtx)
                .inflate(R.layout.card_user, parent, false);
        return new UserViewHolder(userCard);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder holder, final int i) {
        Log.d(TAG, "onBindViewHolder: Lifecycle");

        final String userId = users.get(i).getId();
        final User user = users.get(i);

        // If type is request, show accept and decline buttons.
        if (type == "request") {
            holder.acceptBtn.setVisibility(View.VISIBLE);
            holder.declineBtn.setVisibility(View.VISIBLE);

            holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: Accepted friend request from: " + requestId);
                    onClickListener.respondToRequest(requestId, true);
                }
            });
            holder.declineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: Cancelled friend request from: " + requestId);
                    onClickListener.respondToRequest(requestId, false);
                }
            });
            //
        } else if (type == "add") {
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!selectedUsers.contains(user)) {
                        // orangeSecondary is #e28016
                        // = r226 g128 b22
                        holder.cardView.setCardBackgroundColor(Color.rgb(226, 128, 22));
                        Log.d(TAG, "onClick: added user: " + user.getId());
                        selectedUsers.add(user);
                    } else {
                        // cardview default is #bdc4ca
                        // = r189 g196 b202
                        holder.cardView.setCardBackgroundColor(Color.rgb(189, 196, 202));
                        Log.d(TAG, "onClick: removed user" + user.getId());

                        selectedUsers.remove(user);
                    }
                    onClickListener.selectedUsers(selectedUsers);

                }
            });

            // Remove user from selection.
        } else if (type == "selected") {
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: Removed user: " + userId);
                    users.remove(i);
                    onClickListener.selectedUsers(users);
                }
            });
        }

        Glide.with(uCtx)
                .load(users.get(i).getImage())
                .placeholder(R.drawable.ic_person)
                .into(holder.userImage);

        holder.userName.setText(users.get(i).getFullname());
        holder.userEmail.setText(users.get(i).getEmail());


    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: ");
        if (users != null) {
            return users.size();
        } else {
            return 0;
        }
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userImage;
        private TextView userName;
        private TextView userEmail;
        private Button acceptBtn;
        private Button declineBtn;
        private CardView cardView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "UserViewHolder: Lifecycle");

            this.cardView = itemView.findViewById(R.id.user_card_card_view);
            this.userImage = itemView.findViewById(R.id.user_card_image);
            this.userName = itemView.findViewById(R.id.user_card_name);
            this.userEmail = itemView.findViewById(R.id.user_card_email);
            this.acceptBtn = itemView.findViewById(R.id.user_card_accept);
            this.declineBtn = itemView.findViewById(R.id.user_card_decline);


        }
    }
}
