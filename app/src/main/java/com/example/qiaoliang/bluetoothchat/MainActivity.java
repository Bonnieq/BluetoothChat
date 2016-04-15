package com.example.qiaoliang.bluetoothchat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "MainActivity";
    private static int sBTState = -1;

    private final int REQUES_BT_ENABLE_CODE = 123;
    private final int REQUES_SELECT_BT_CODE = 222;

    private ListView mListview;
    private EditText mInput;
    private Button mSendBtn;
    private BluetoothDevice mRemoteDevice;

    private ArrayAdapter<String> mAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<String> mChatContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getOverflowMenu();
        //UI initialize
        mListview = (ListView)findViewById(R.id.listview1);//chat content layout
        mInput = (EditText)findViewById(R.id.input);//input box

        mChatContent = new ArrayList<String>();
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mChatContent );

        mListview.setAdapter(mAdapter);
        mSendBtn = (Button) findViewById(R.id.button_send);

        mSendBtn.setOnClickListener(this);
        openBTDevice();
    }

    public void onClick(View view){
        String msg= mInput.getText().toString().trim();
        if (msg.length()<=0){
            Toast.makeText(this, "Please input message!", Toast.LENGTH_SHORT).show();
            return;
        }
        mChatContent.add(mBluetoothAdapter.getName() + ":" + msg);
        mAdapter.notifyDataSetChanged();
        TaskService.newTask(new Task(mHandler,Task.TASK_SEND_MSG, new Object[]{msg}));
        SaveToCsv(mChatContent, mBluetoothAdapter.getName(), getApplicationContext());
        mInput.setText("");

    }
    private Boolean openBTDevice() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Your device does not support Bluetooth!");
            Toast.makeText(this, "Your device does not support Bluetooth!", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()){
            //use intent to enable bluetooth
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUES_BT_ENABLE_CODE);
        }else{
            startServiceAsServer();
        }
        return true;
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void startServiceAsServer() {
        //asynchronous communication
        TaskService.start(this, mHandler);
        TaskService.newTask(new Task(mHandler, Task.TASK_START_ACCEPT, null));
    }
    private Handler mHandler = new Handler(){
        public void handlerMessage(Message msg){
            switch (msg.what){
                case Task.TASK_SEND_MSG:
                    Toast.makeText(MainActivity.this, msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
                case Task.TASK_RECV_MSG:
                    mChatContent.add(msg.obj.toString());
                    SaveToCsv(mChatContent, SelectDevice.targetDev, getApplicationContext());
                    mAdapter.notifyDataSetChanged();
                    break;
                case Task.TASK_GET_REMOTE_STATE:
                    setTitle(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUES_BT_ENABLE_CODE && resultCode==RESULT_OK){
            startServiceAsServer();
        }else if (requestCode==REQUES_SELECT_BT_CODE && resultCode==RESULT_OK){
            mRemoteDevice=data.getParcelableExtra("DEVICE");
            if (mRemoteDevice==null)
                return;
            TaskService.newTask(new Task(mHandler, Task.TASK_START_CONN_THREAD,new Object[]{}));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.name:
                AlertDialog.Builder dlg=new AlertDialog.Builder(this);
                final EditText nameEdit= new EditText(this);
                dlg.setView(nameEdit);
                dlg.setTitle("Set user name:");
                dlg.setPositiveButton("OK", new OnClickListener(){
                    public void onClick(DialogInterface dlg, int which){
                        if (nameEdit.getText().toString().length()!=0)
                            mBluetoothAdapter.setName(nameEdit.getText().toString());

                    }
                });
                dlg.create();
                dlg.show();
                break;
            case R.id.scan:
                startActivityForResult(new Intent(this, SelectDevice.class), REQUES_SELECT_BT_CODE);
                break;
            case R.id.history:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, History.class);
                startActivity(intent);
        }
        return true;
    }
    public void SaveToCsv(ArrayList<String> content, String name, Context context){
        PrintWriter csvWriter;
        try
        {

            File file = new File(context.getFilesDir(),name);
            if(!file.exists()){
                file = new File(context.getFilesDir(),name);
            }
            csvWriter = new  PrintWriter(new FileWriter(file,true));


            csvWriter.print(content.toString());
            csvWriter.print("\r\n");
            csvWriter.close();


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
