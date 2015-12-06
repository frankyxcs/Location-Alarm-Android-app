package com.keerthan.locationalarm2.Alarms;

/**
 * Created by Keerthan on 07-Dec-15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.keerthan.locationalarm2.Constants;

import java.io.IOException;

public class SQLstorage extends SQLiteOpenHelper {

    public static final String TAG = "SQLstorage";

    public SQLstorage(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public SQLstorage(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public SQLstorage(Context context){
        super(context, Constants.DBNAME, null, Constants.DBVERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQLcommand = "CREATE TABLE alarms " +
                "(title TEXT PRIMARY KEY, latitude DOUBLE PRECISION, longitude DOUBLE PRECISION, " +
                "enabled TEXT, range FLOAT, ringtoneUri TEXT) ";
        db.execSQL(SQLcommand);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS alarms");
        onCreate(db);
    }

    public boolean saveAlarm(Alarm alarm){
        Log.d(TAG, "attempting to save the alarm");
        if(alarm == null)
            return false;

        String title = alarm.getName();
        Double latitude = alarm.getPosition().latitude;
        Double longitude = alarm.getPosition().longitude;
        Boolean enabled = alarm.isActive();
        Float range = alarm.getRange();
        String ringtoneUri = alarm.getRingtoneUri().toString();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.DB_TITLE, title);
        contentValues.put(Constants.DB_LATITUDE, latitude);
        contentValues.put(Constants.DB_LONGITUDE, longitude);
        contentValues.put(Constants.DB_ENABLED, enabled.toString());
        contentValues.put(Constants.DB_RANGE, range);
        contentValues.put(Constants.DB_RINGTONE_URI, ringtoneUri);

        long result = db.insert(Constants.DBNAME, null, contentValues );
        if(result == -1){
            Log.d(TAG, "save attempt unsuccessful");
            return false;
        }
        Log.d(TAG, "save attempt successful");
        return true;
    }

    public Cursor getTable(){
        Log.d(TAG, "inside getTable");
        SQLiteDatabase db = this.getReadableDatabase();
        String SQLcommand = "SELECT * FROM alarms";
        return db.rawQuery(SQLcommand, new String[] {} );
    }

    public Cursor retrieveAlarm(String title, Double latitude, Double longitude){
        SQLiteDatabase db = this.getReadableDatabase();
        String SQLcommand = "SELECT * FROM alarms WHERE title=? AND latitude=? AND longitude=?";
        return db.rawQuery(SQLcommand, new String[]{title, latitude.toString(), longitude.toString()});
    }
}
