package com.example.qiaoliang.bluetoothchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.net.URL;
import java.util.ArrayList;
import java.util.Set;


public class History extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ArrayAdapter<String> adapter;
    private ListView DevicesHistory;
    private ArrayList<String> mArrayAdapter = new ArrayList<String>();
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        DevicesHistory = (ListView) findViewById(R.id.device_history);

        DevicesHistory.setOnItemClickListener(this);
        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                mArrayAdapter);

        DevicesHistory = SelectDevice.mDevList;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        EditText address=(EditText)findViewById(R.id.email_address);
        String targetDev = mArrayAdapter.get(arg2);
        System.out.println(targetDev);
        String deviceName=mDeviceList.get(arg2).getName();
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
// The intent does not have a URI, so declare the "text/plain" MIME type
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {address.getText().toString()}); // recipients
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, deviceName+"'s chat history.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(getApplicationContext().getFilesDir()+"/"+deviceName));
// You can also attach multiple items by passing an ArrayList of Uris
    }
}
