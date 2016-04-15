package com.example.qiaoliang.bluetoothchat;

import java.util.ArrayList;
import java.util.Set;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View.OnClickListener;


public class SelectDevice extends AppCompatActivity implements OnClickListener, OnItemClickListener {
    private final String TAG = "MainActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private Button mScanBtn;
    public static ListView mDevList;
    public static String targetDev;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> mArrayAdapter = new ArrayList<String>();
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_select_device);

        mDevList = (ListView) findViewById(R.id.device_list);

        mDevList.setOnItemClickListener(this);

        mScanBtn = (Button) findViewById(R.id.scanBtn);
        mScanBtn.setOnClickListener(this);

        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                mArrayAdapter);

        mDevList.setAdapter(adapter);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.e(TAG, "Your device is not support Bluetooth!");
            return;
        }

        // if the bluetooth is off, request for turning on
        if (!mBluetoothAdapter.isEnabled()) {
            // request for turning on
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 21);
        }else{
            findDevice();
        }



        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

    }
    @Override
    public void onClick(View view) {
        if(!mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.startDiscovery();
            // 开始显示进度
            setProgressBarIndeterminateVisibility(true);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 已包含该设备
                if(mDeviceList.contains(device)){
                    return;
                }
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                System.out.println(device.getName() + "\n" + device.getAddress());
                mDeviceList.add(device);
                adapter.notifyDataSetChanged();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                // 取消扫描进度显示
                setProgressBarIndeterminateVisibility(false);
            }
        }
    };
    private void findDevice(){
        // 获得已经保存的配对设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mDeviceList.add(device);
            }
        }

        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 21){
            if(resultCode ==  RESULT_OK){
                System.out.println("open");
                findDevice();
            }else{
                System.out.println("fail to open");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        targetDev = mArrayAdapter.get(arg2);
        System.out.println(targetDev);
        // pass the targetDev to the main activity
        Intent data = new Intent();
        data.putExtra("DEVICE", mDeviceList.get(arg2));
        setResult(RESULT_OK, data);
        this.finish();
    }
}
