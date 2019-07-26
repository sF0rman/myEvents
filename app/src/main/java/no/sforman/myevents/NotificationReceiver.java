package no.sforman.myevents;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String TAG = "NotificationReciever";

    private String id;
    private String reminder;
    private String name;
    private String msg;
    private String nChannel;

    @Override
    public void onReceive(Context context, Intent intent) {

        id = (String) intent.getStringExtra("eventId");
        reminder = (String) intent.getStringExtra("reminder");
        name = (String) intent.getStringExtra("name");
        msg = (String) intent.getStringExtra("message");
        nChannel = (String) intent.getStringExtra("channel");

        Intent notificationIntent = new Intent(context, EventActivity.class);
        notificationIntent.putExtra("eventId", id);
        notificationIntent.putExtra("reminder", reminder);
        Log.d(TAG, "onReceive: Got ID" + id);

        PendingIntent contentIntent = PendingIntent.getActivity(context,
                (int) System.currentTimeMillis(),
                notificationIntent,
                0);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, nChannel)
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle(name)
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        notificationManager.notify(0, builder.build());

    }
}
