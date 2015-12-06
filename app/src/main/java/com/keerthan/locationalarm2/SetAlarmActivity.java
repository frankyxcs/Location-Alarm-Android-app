package com.keerthan.locationalarm2;


import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.model.LatLng;
import com.keerthan.locationalarm2.Alarms.Alarm;

import java.io.IOException;

public class SetAlarmActivity extends AppCompatActivity {

    static final String TAG = "SetAlarmActivity";

    Alarm alarm = null;
    Uri ringtoneUri = null;
    Ringtone ringtone = null;

    EditText label;
    CheckBox enable;
    ToggleButton repeat;
    TextView rangeText;
    SeekBar rangeSeekBar;
    Button selectRingtone;
    Button saveBtn;
    Button cancelBtn;

    float range;
    LatLng position;

    TextView latitudeTV;
    TextView longitudeTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_alarm_activity);
        Log.d(TAG, "onCreate");
        Intent intent = getIntent();
        position = new LatLng(intent.getDoubleExtra(Constants.SEND_LATITUDE, 0), intent.getDoubleExtra(Constants.SEND_LONGITUDE, 0));

        ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this, 0);
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri);

        label = (EditText) findViewById(R.id.ET_title);
        enable = (CheckBox) findViewById(R.id.CB_activate);
        repeat = (ToggleButton) findViewById(R.id.TB_activate_button);
        rangeSeekBar = (SeekBar) findViewById(R.id.SB_range);
        rangeText = (TextView) findViewById(R.id.TV_range);
        selectRingtone = (Button) findViewById(R.id.Button_ringtone);
        latitudeTV = (TextView) findViewById(R.id.TV_latitude);
        longitudeTV = (TextView) findViewById(R.id.TV_longitude);

        saveBtn = (Button) findViewById(R.id.BT_save);
        cancelBtn = (Button) findViewById(R.id.BT_cancel);

        range = 0.1f;

        String latitude = "Latitude : " + String.valueOf(position.latitude);
        String longitude = "Longitude : " + String.valueOf(position.longitude);
        latitudeTV.setText(latitude);
        longitudeTV.setText(longitude);

        setListeners();
        setupRingtonePicker();

    }

    public void setListeners(){
        Log.d(TAG, "Setting the listeners");
        rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                range = progress * 0.099f + 0.1f;

                if (range < 1) {
                    String metres = String.valueOf(range * 1000);
                    metres = metres + " metres";
                    rangeText.setText(metres);
                } else {
                    String kilometres = String.valueOf(range);
                    kilometres = kilometres + " kilometres";
                    rangeText.setText(kilometres);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                alarm.setIsActive(isChecked);
            }
        });


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm = new Alarm(position, range, label.getText().toString(), ringtoneUri);
                alarm.setIsActive(enable.isChecked());
                try {
                    alarm.SaveToInternal(getApplicationContext());
                    Toast.makeText(getApplicationContext(), "Alarm has been saved", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });

    }

    void setupRingtonePicker(){
        Log.d(TAG,"Setting the ringtone picker");
        Uri defaultUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        ringtoneUri = defaultUri;
        if(defaultUri !=null)
            selectRingtone.setText(defaultUri.getUserInfo());
        selectRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseRingtone();
            }
        });
    }

    void chooseRingtone(){
        Log.d(TAG, "inside chooseRingtone");
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone : ");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        startActivityForResult(intent, 999);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Uri chosenRingtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if(chosenRingtone != null){
                ringtoneUri = chosenRingtone;
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM, ringtoneUri);
                Log.d(TAG, "back to activity with chosen ringtone - " + chosenRingtone.toString());
            }
        }
    }


}