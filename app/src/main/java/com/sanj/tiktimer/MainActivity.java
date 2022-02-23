package com.sanj.tiktimer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.sanj.tiktimer.threads.CountDown;
import com.sanj.tiktimer.threads.CountUP;
import com.sanj.tiktimer.threads.ThreadConstants;

public class MainActivity extends AppCompatActivity {
    TextInputEditText edHrs, edMin, edSec;
    private Button btnStart;
    private TextView txtTimer;
    private ProgressBar progressTimer;
    private String[] timerType;
    private int selectedType;
    private CountDown countDown;
    private CountUP countUP;
    private boolean typeChosen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id.btnStart);
        txtTimer = findViewById(R.id.txtTimer);
        progressTimer = findViewById(R.id.progressTimer);
        progressTimer.invalidate();
        progressTimer.setIndeterminate(false);

        timerType = new String[]{
                "Count Down",
                "Count Up"
        };
        selectedType = 0;
        ThreadConstants.isTaskRunning = false;
        typeChosen=false;

        btnStart.setOnClickListener(v -> {
            if (!typeChosen){
                chooseTimerType();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void chooseTimerType() {
        typeChosen=true;
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Timer Type");
        builder.setSingleChoiceItems(timerType, selectedType, (dialog, which) -> {
            selectedType = which;
            dialog.dismiss();
            getTime();
        });
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();

    }

    private void getTime() {
        LayoutInflater layoutInflater = getLayoutInflater();
        @SuppressLint("InflateParams") View getTimeLayout = layoutInflater.inflate(R.layout.get_time_layout, null);
        Button btnLaunch;
        edHrs = getTimeLayout.findViewById(R.id.edHrs);
        edMin = getTimeLayout.findViewById(R.id.edMin);
        edSec = getTimeLayout.findViewById(R.id.edSec);
        btnLaunch = getTimeLayout.findViewById(R.id.btnLaunch);

        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getTimeLayout);
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();

        btnLaunch.setOnClickListener(v -> {
            alertDialog.dismiss();
            launchTimer();
        });
    }

    private void launchTimer() {
        typeChosen=false;
        String stringifyHrs, stringifyMin, stringifySec;
        int hrs, min, sec, convertedToSec;

        stringifyHrs = !edHrs.getText().toString().trim().equals("") ? edHrs.getText().toString().trim() : "0";
        stringifyMin = !edMin.getText().toString().trim().equals("") ? edMin.getText().toString().trim() : "0";
        stringifySec = !edSec.getText().toString().trim().equals("") ? edSec.getText().toString().trim() : "0";

        hrs = Integer.parseInt(stringifyHrs);
        min = Integer.parseInt(stringifyMin);
        sec = Integer.parseInt(stringifySec);

        convertedToSec = (hrs * 3600) + (min * 60) + (sec);

        if (convertedToSec > 0) {
            switch (selectedType) {
                case 0:
                    countDown = new CountDown(btnStart, txtTimer, progressTimer, convertedToSec, this);
                    countDown.execute();
                    ThreadConstants.isTaskRunning = true;
                    break;
                case 1:
                    countUP = new CountUP(btnStart, txtTimer, progressTimer, convertedToSec, this);
                    countUP.execute();
                    ThreadConstants.isTaskRunning = true;
                    break;
            }
        } else {
            AlertDialog alertDialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("TIKTIMER");
            builder.setMessage("Oops! Are you aware you just submitted nothing?");
            builder.setPositiveButton("YES", (dialog, which) -> {
                customToast("Ooh, I won't do anything either");
                dialog.dismiss();
            });
            builder.setNegativeButton("NO", (dialog, which) -> {
                customToast("Understandable, Redeem yourself then please");
                getTime();
            });
            builder.setCancelable(false);
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void customToast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        View view = toast.getView();
        view.getBackground().setColorFilter(this.getResources().getColor(R.color.devThrone), PorterDuff.Mode.SRC_IN);
        toast.setGravity(Gravity.TOP, 0, 90);
        TextView textView = view.findViewById(android.R.id.message);
        textView.setTextColor(this.getResources().getColor(R.color.white));
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                startActivity(new Intent(this,About.class));
                break;
            case R.id.menu_refresh:
                if (ThreadConstants.isTaskRunning) {
                    switch (selectedType) {
                        case 0:
                            countDown.cancel(true);
                            ThreadConstants.isTaskRunning = false;
                            break;
                        case 1:
                            countUP.cancel(true);
                            ThreadConstants.isTaskRunning = false;
                            break;
                    }
                } else {
                    customToast("There is no TikTimer task running");
                }
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ThreadConstants.isApplicationOpen = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        ThreadConstants.isApplicationOpen=false;
    }
}