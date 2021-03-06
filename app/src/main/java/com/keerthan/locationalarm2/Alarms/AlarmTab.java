package com.keerthan.locationalarm2.Alarms;

import android.support.v4.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.keerthan.locationalarm2.R;

import java.util.LinkedList;
import java.util.List;

public class AlarmTab extends android.support.v4.app.ListFragment {

    private static final String TAG = "AlarmTab";

    private ListView alarmListView;
    private ListAdapter alarmListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView in AlarmTab");
        updateList();
        setListAdapter(alarmListAdapter);
        return inflater.inflate(R.layout.alarm_screen, container, false);
    }

    private void updateList(){
        Log.i(TAG, "updateList");
        LinkedList<Alarm> alarmLinkedList =  Alarm.RetrieveList(getActivity().getApplicationContext());

        if(alarmLinkedList!=null) {
            Alarm[] alarms = convertLinkedListtoArray(alarmLinkedList);
            alarmListAdapter = new MyAdapter(getActivity(), R.layout.alarm_row_layout, alarms);
           // setupListView();
            Log.d(TAG, "Successfully placed non-null linked list on listview");
        }
        else{
            Log.e(TAG, "LinkedList is empty.");
            alarmListAdapter = new ArrayAdapter<Alarm>(getActivity(), R.layout.alarm_row_layout);
        }
    }

    private Alarm[] convertLinkedListtoArray(LinkedList<Alarm> alarmLinkedList){
        Alarm[] alarms = new Alarm[alarmLinkedList.size()];
        int i=0;
        while(alarmLinkedList.size()!=0){
            alarms[i++] = alarmLinkedList.removeFirst();
        }
        return alarms;
    }

    /*private void setupListView(){
        alarmListView.setAdapter(alarmListAdapter);
    }*/

    @Override
    public void onStart() {
        super.onStart();
        updateList();
    }
}

class MyAdapter extends ArrayAdapter<Alarm>{

    boolean nonZeroAlarms = false;

    public MyAdapter(Context context, int resource)
    {
        super(context, resource);
        nonZeroAlarms = true;
    }

    public MyAdapter(Context context, int resource, List<Alarm> objects) {
        super(context, resource, objects);
    }

    public MyAdapter(Context context, int resource, Alarm[] objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(nonZeroAlarms){
            return getAlarmLayout(position, parent);
        }

        return noAlarmsLayout(position);
    }

    private View getAlarmLayout(int position, ViewGroup parent){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View row = layoutInflater.inflate(R.layout.alarm_row_layout, parent, false);
        Alarm alarm = getItem(position);

        if(alarm==null){
            return row;
        }

        TextView alarmTitle = (TextView) row.findViewById(R.id.TV_alarm_title);
        alarmTitle.setText(alarm.getName());

        TextView alarmLatLng = (TextView) row.findViewById(R.id.TV_alarm_LatLng);
        String location = "Latitude: " + String.valueOf(alarm.getPosition().latitude)
                + " Longitude: " + String.valueOf(alarm.getPosition().longitude);
        alarmLatLng.setText(location);

        ToggleButton toggleButton = (ToggleButton) row.findViewById(R.id.TB_activate_button);
        if(alarm.isActive()){
            toggleButton.setChecked(true);
        } else {
            toggleButton.setChecked(false);
        }

        return row;
    }

    private View noAlarmsLayout(int position){
        TextView textView = new TextView(getContext());
        textView.setText("You have not set any alarms yet. Long click on the map to add one now!");
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        return textView;
    }
}
