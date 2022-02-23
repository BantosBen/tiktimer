package com.sanj.tiktimer.threads;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sanj.tiktimer.MainActivity;
import com.sanj.tiktimer.R;

import static java.lang.Thread.sleep;

public class CountUP extends AsyncTask<Void,Integer,Boolean> {
    @SuppressLint("StaticFieldLeak")
    private final Button btnStart;
    @SuppressLint("StaticFieldLeak")
    private final TextView txtTimer;
    @SuppressLint("StaticFieldLeak")
    private final ProgressBar progressTimer;

    private int convertedToSec,progressSec;
    @SuppressLint("StaticFieldLeak")
    private final Context mContext;
    private final String NOTIFICATION_CHANNEL_ID = "10001";

    public CountUP(Button btnStart, TextView txtTimer, ProgressBar progressTimer,int convertedToSec,Context mContext) {
        this.btnStart = btnStart;
        this.txtTimer = txtTimer;
        this.progressTimer = progressTimer;
        this.convertedToSec=convertedToSec;
        this.mContext=mContext;
        progressSec=0;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        try {
            while (convertedToSec!=progressSec){
                if (!isCancelled()){
                    publishProgress(1);
                    sleep(1000);
                    ++progressSec;
                }else{
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        btnStart.setVisibility(View.GONE);
        txtTimer.setVisibility(View.VISIBLE);
        progressTimer.setVisibility(View.VISIBLE);

        txtTimer.setText(structureTime(progressSec));
        progressTimer.setMax(convertedToSec);
        progressTimer.setProgress(progressSec);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        notifyUser();
        btnStart.setVisibility(View.VISIBLE);
        txtTimer.setVisibility(View.GONE);
        progressTimer.setVisibility(View.GONE);
        ThreadConstants.isTaskRunning = false;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (values[0] == 1) {
            txtTimer.setText(structureTime(progressSec));
            progressTimer.setProgress(progressSec);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        btnStart.setVisibility(View.VISIBLE);
        txtTimer.setVisibility(View.GONE);
        progressTimer.setVisibility(View.GONE);
        ThreadConstants.isTaskRunning = false;
    }

    private String structureTime(int convertedToSec){
        if (convertedToSec>59){
            int minutes=convertedToSec/60;
            if (minutes>59){
                int hours=convertedToSec/3600;
                minutes=(convertedToSec-(hours*3600))/60;
                int seconds=convertedToSec-((hours*3600)+(minutes*60));

                return hours+" : "+minutes+" : "+seconds;
            }else {
                int seconds=convertedToSec-(minutes*60);
                return minutes+" : "+seconds;
            }
        }else{
            return String.valueOf(convertedToSec);
        }
    }

    private void notifyUser(){
        Intent intent;
        if (ThreadConstants.isApplicationOpen){
            intent=new Intent();
        }else{
            intent=new Intent(mContext, MainActivity.class);
        }

        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext,NOTIFICATION_CHANNEL_ID)
                .setContentTitle("TIKTIMER")
                .setContentText("Task Complete")
                .setSmallIcon(R.mipmap.logo)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel;
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "TIKTIMER", importance);
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel((notificationChannel));
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(0, mBuilder.build());
    }
}

