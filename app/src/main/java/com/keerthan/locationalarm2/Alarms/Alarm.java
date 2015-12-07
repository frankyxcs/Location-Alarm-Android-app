package com.keerthan.locationalarm2.Alarms;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.GpsStatus;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.MediaRouteActionProvider;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.keerthan.locationalarm2.Constants;
import com.keerthan.locationalarm2.MainActivity;
import com.keerthan.locationalarm2.R;
import com.keerthan.locationalarm2.SetAlarmActivity;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Alarm implements Serializable { // It inherits from the Serializable class so as to convert the objects into stream of bytes

    private static final String TAG = "Alarm";

    private LatLng position;
    private float range;

    private Ringtone ringtone;
    private Uri ringtoneUri;
    private String name;

    private boolean isActive;

    public Alarm(LatLng position, float range, String name, Uri ringtoneUri) {
        this.position = position;
        this.range = range;
        this.name = name;
        this.ringtoneUri = ringtoneUri;
        isActive = true;
    }

    public Uri getRingtoneUri(){ return ringtoneUri; }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public Ringtone getRingtone() {
        return ringtone;
    }

    public void setRingtone(Ringtone ringtone) {
        this.ringtone = ringtone;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {   this.isActive = isActive;}

    public boolean SaveToInternal(Context context) throws IOException {
        //InternalStorage.writeObject(context, Constants.ALARM_LISTFILE, this);
        SQLstorage storage = new SQLstorage(context);
        return storage.saveAlarm(this);
    }

    /*public static Alarm Retrieve(Context context) throws IOException, ClassNotFoundException {
        return (Alarm) InternalStorage.readObject(context, Constants.ALARM_LISTFILE);
    }*/

    /*public Alarm Retrieve(Context context, String title, String){
        SQLstorage storage = new SQLstorage(context);
        Cursor cursor = storage.retrieveAlarm(this.getName(), this.getPosition().latitude, this.getPosition().longitude);
        int numberOfMatches = cursor.getCount();
        if(numberOfMatches != 0){
            //match found!


        }
    }*/


    public static void setAlarm(Context context, LatLng position){
        //needs to be completed
        //probably take to the new activity for setting an alarm?
        //Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, 1);
        //Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
        //Alarm alarm = new Alarm(position, 0.1f, "", ringtone);
        Intent intent = new Intent(context, SetAlarmActivity.class);
        //intent.putExtra(Constants.SET_NEW_ALARM, alarm);
        intent.putExtra(Constants.SEND_LATITUDE,position.latitude);
        intent.putExtra(Constants.SEND_LONGITUDE,position.longitude);
        context.startActivity(intent);

    }

    public static LinkedList<Alarm> RetrieveList(Context context){
        //needs to be completed
        Log.d(TAG,"RetrieveList - Retrieving list of alarms");
        LinkedList<Alarm> list = null;
        SQLstorage sqLstorage = new SQLstorage(context);
        Cursor cursor = sqLstorage.getTable();
        if(cursor.getCount()> 0){
            //table is nonempty
            int rows = cursor.getCount();
            int columns = cursor.getColumnCount();
            while(!cursor.isAfterLast()){
                String title = cursor.getString(cursor.getColumnIndex(Constants.DB_TITLE));
                Double latitude = cursor.getDouble(cursor.getColumnIndex(Constants.DB_LATITUDE));
                Double longitude = cursor.getDouble(cursor.getColumnIndex(Constants.DB_LONGITUDE));
                Boolean enabled = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Constants.DB_ENABLED)));
                Float range = cursor.getFloat(cursor.getColumnIndex(Constants.DB_RANGE));
                Uri ringtoneUri = Uri.parse(cursor.getString(cursor.getColumnIndex(Constants.DB_RINGTONE_URI)));

                LatLng position = new LatLng(latitude, longitude);
                Alarm alarm = new Alarm(position, range, title, ringtoneUri);
                alarm.setIsActive(enabled);

                list.add(alarm);

                cursor.moveToNext();
            }
        }
        return list;
    }

    public static void triggerAlarms(List<String> AlarmIDs, Context ActivityContext){
        LinkedList<Alarm> alarmLinkedList = Alarm.RetrieveList(ActivityContext);
        for(Alarm alarm : alarmLinkedList){
            boolean triggered = false;

            if(AlarmIDs.size()!=0) {
                for (String Name : AlarmIDs) {
                    if (alarm.getName() == Name) {
                        triggered = true;
                        alarm.ring(ActivityContext);
                        AlarmIDs.remove(Name);
                        break;
                    }
                }
            } else {
                break;
            }
        }
    }

    private void ring(Context ActivityContext){
        sendNotification(name, ActivityContext);
        startRingtonePlayback(ActivityContext);
        showDialog();
    }

    private void sendNotification(String notificationDetails, Context ActivityContext) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(ActivityContext.getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ActivityContext);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ActivityContext);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.notification_template_icon_bg)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                //        R.drawable.notification_template_icon_bg))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(this.getName())
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) ActivityContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    protected void startRingtonePlayback(Context ActivityContext){
        ringtone = RingtoneManager.getRingtone(ActivityContext,ringtoneUri);
        ringtone.play();
    }

    protected void showDialog(){

    }
}