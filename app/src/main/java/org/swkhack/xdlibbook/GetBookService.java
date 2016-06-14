package org.swkhack.xdlibbook;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by swk on 2/28/16.
 */
public class GetBookService extends Service {
    public static MyDatabaseHelper dbHelper;
    public static getBookDateTask myGet;
    public static final int getData_success = 1;
    public static final int getData_failed = 0;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("swkDebug2", "In Service");
        myGet.execute();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long aDay = 24 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + aDay;
        Intent i = new Intent(this, GetBookReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }


}
