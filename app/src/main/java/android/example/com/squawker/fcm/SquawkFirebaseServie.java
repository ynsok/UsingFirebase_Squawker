package android.example.com.squawker.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by Krzys on 08.03.2018.
 */

public class SquawkFirebaseServie extends FirebaseMessagingService {

    private static final String AUTHOR = SquawkContract.COLUMN_AUTHOR;
    private static final String AUTHOR_KEY = SquawkContract.COLUMN_AUTHOR_KEY;
    private static final String DATE = SquawkContract.COLUMN_DATE;
    private static final String MESSAGE = SquawkContract.COLUMN_MESSAGE;

    private static final int REQUEST_CODE =1;
    private static final int BEGIN_INDEX = 0;
    private static final int END_INDEX = 30;
    private static final int NOTIFICATION_ID =0;
    private static final String TAG = "SquawkFireBaseServie";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> receivedData = remoteMessage.getData();

        if (receivedData.size() > 0)
        {
            Log.i(TAG, "onMessageReceived: "+receivedData);
            sendNotification(receivedData);
            insertIntoProvider(receivedData);
        }
           }

    void  insertIntoProvider(final Map<String,String>data)
    {
        AsyncTask<Void,Void,Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(SquawkContract.COLUMN_AUTHOR_KEY,data.get(AUTHOR_KEY));
                contentValues.put(SquawkContract.COLUMN_AUTHOR,data.get(AUTHOR));
                contentValues.put(SquawkContract.COLUMN_DATE,data.get(DATE));
                contentValues.put(SquawkContract.COLUMN_MESSAGE,data.get(MESSAGE));
                getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI,contentValues);

                return null;
            }
        };

        asyncTask.execute();
    }
    void sendNotification(Map<String,String>data)
    {
        String message = data.get(MESSAGE);
        String author = data.get(AUTHOR);

        Intent intent  = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        PendingIntent pendingIntent = PendingIntent.getActivity(this,REQUEST_CODE,intent,PendingIntent.FLAG_ONE_SHOT);


        if(message.length()>30)
        {
           message= message.substring(BEGIN_INDEX,END_INDEX)+"\u2026";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                                    builder.setContentTitle(author);
                                    builder.setContentText(message);
                                    builder.setAutoCancel(true);
                                    builder.setSmallIcon(R.drawable.ic_duck);
                                    builder.setContentIntent(pendingIntent);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID,builder.build());

    }
}
