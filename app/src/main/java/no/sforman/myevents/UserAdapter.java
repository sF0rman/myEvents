package no.sforman.myevents;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public static final String TAG = "UserAdapter";

    private Context uCtx;
    private boolean friendRequest = false;
    private boolean addUser = false;

    private ArrayList<User> users;

    public UserAdapter(Context context, ArrayList<User> users){
        this.uCtx = context;
        this.users = users;
    }

    public UserAdapter(Context context, ArrayList<User> users, boolean friendRequest){
        this.uCtx = context;
        this.users = users;
        this.friendRequest = friendRequest;
    }

    public UserAdapter(Context context, boolean addUser, ArrayList<User> users){
        this.uCtx = context;
        this.users = users;
        this.addUser = addUser;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View userCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user, parent, false);
        UserViewHolder viewHolder = new UserViewHolder(userCard);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder holder, final int i) {
        Log.d(TAG, "onBindViewHolder: Started");

        final String userId = users.get(i).getId();

        if(friendRequest){
            holder.acceptBtn.setVisibility(View.VISIBLE);
            holder.declineBtn.setVisibility(View.VISIBLE);

            holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: Accepted friendrequest from: " + userId);
                }
            });
            holder.declineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: Cancelled friendrequest from: " + userId);
                }
            });
        } else if (addUser) {
            final ArrayList<String> userIds = new ArrayList<>();
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isSelected = false;

                    // Check if already selected
                    int index = 0;
                    for (String uId : userIds) {
                        String id = users.get(i).getId();
                        if (id.equals(uId)) {
                            Log.d(TAG, "onClick: removed user: " + userIds.get(index));
                            userIds.remove(index);

                            // orangeSecondary is #e28016
                            // = r226 g128 b22
                            holder.cardView.setBackgroundColor(Color.rgb(226, 128, 22));
                            isSelected = true;
                        }
                        index++;
                    }

                    if(!isSelected){
                        // cardview default is #bdc4ca
                        // = r189 g196 b202
                        holder.cardView.setBackgroundColor(Color.rgb(189, 196, 202));
                        Log.d(TAG, "onClick: added user: " + users.get(i).getId());
                        userIds.add(users.get(i).getId());
                    }

                }
            });
        }

        Glide.with(uCtx)
                .load(users.get(i).getImgUrl())
                .into(holder.userImage);

        holder.userName.setText(users.get(i).getFullname());
        holder.userEmail.setText(users.get(i).getEmail());


    }

    @Override
    public int getItemCount() {
        if(users != null){
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

        public UserViewHolder(@NonNull View itemView){
            super(itemView);

            this.cardView = itemView.findViewById(R.id.user_card_card_view);
            this.userImage = itemView.findViewById(R.id.user_card_image);
            this.userName = itemView.findViewById(R.id.user_card_name);
            this.acceptBtn = itemView.findViewById(R.id.user_card_accept);
            this.declineBtn = itemView.findViewById(R.id.user_card_decline);



        }
    }
}
