package no.sforman.myevents;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

public class App extends Application {

    public static final String TAG = "App";

    public static final String EVENT_CHANNEL = "event";
    public static final String INVITE_CHANNEL = "invite";
    public static final String FRIEND_CHANNEL = "friend";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel: Lifecycle");
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            CharSequence ename = getString(R.string.channel_event);
            CharSequence iname = getString(R.string.channel_invite);
            CharSequence fname = getString(R.string.channel_friend);
            String edesc = getString(R.string.channel_event_description);
            String idesc = getString(R.string.channel_invite_description);
            String fdesc = getString(R.string.channel_friend_description);

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel eventChannel = new NotificationChannel(
                    EVENT_CHANNEL,
                    ename,
                    importance);
            eventChannel.setDescription(edesc);

            NotificationChannel inviteChannel = new NotificationChannel(
                    INVITE_CHANNEL,
                    iname,
                    importance);
            inviteChannel.setDescription(idesc);

            NotificationChannel friendChannel = new NotificationChannel(
                    FRIEND_CHANNEL,
                    fname,
                    importance
            );
            friendChannel.setDescription(fdesc);


            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(eventChannel);
            notificationManager.createNotificationChannel(inviteChannel);
            notificationManager.createNotificationChannel(friendChannel);
        }
    }

}
