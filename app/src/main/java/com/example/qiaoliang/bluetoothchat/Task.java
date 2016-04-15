package com.example.qiaoliang.bluetoothchat;

import android.os.Handler;
/**
 * Created by qiaoliang on 16/4/8.
 */
public class Task {
    public static final int TASK_START_ACCEPT =1;
    public static final int TASK_START_CONN_THREAD =2;
    public static final int TASK_SEND_MSG = 3;
    public static final int TASK_GET_REMOTE_STATE =4;
    public static final int TASK_RECV_MSG =5;

    private int mTaskId;//task id
    public Object[] mParams;
    private Handler mhandler;

    public Task(Handler handler, int taskId, Object[] params){
        this.mhandler=handler;
        this.mTaskId= taskId;
        this.mParams = params;
    }
    public Handler getHandler(){
        return this.mhandler;
    }

    public int getTaskID(){
        return mTaskId;
    }
}
