package com.mad.p03.np2020.routine.models;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mad.p03.np2020.routine.R;

import java.util.Calendar;

/**
 *
 * PopUp class used to manage card activities
 *
 * @author Pritheev
 * @since 02-06-2020
 *
 */
public class PopUp extends Activity {
    //Initializing variables

    //Used for hours add button
    ImageButton UpArrowLeft;

    //Used for minutes add button
    ImageButton UpArrowRight;

    //Used for hours reduce button
    ImageButton DownArrowLeft;

    //Used for minutes reduce button
    ImageButton DownArrowRight;

    //Used for Set Timer Button
    Button SetTimer;

    //Used for hours timer
    TextView TimerLeft;

    //Used for minutes timer
    TextView TimerRight;

    //Initializing hours variable
    public int hours = 0;

    //Initializing minutes variable
    public int minutes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupwindow);

        createNotificationChannel();

        //Identifying hours add button
        UpArrowLeft = findViewById(R.id.LeftTop);

        //Identifying minutes add button
        UpArrowRight = findViewById(R.id.RightTop);

        //Identifying hours reduce button
        DownArrowLeft = findViewById(R.id.LeftDown);

        //Identifying minutes reduce button
        DownArrowRight = findViewById(R.id.RightDown);

        //Identifying Set Timer Button
        SetTimer = findViewById(R.id.setTimer);

        //Identifying hours timer text view
        TimerLeft = findViewById(R.id.timerLeft);

        //Identifying minutes timer text view
        TimerRight = findViewById(R.id.timerRight);

        final Calendar calendar = Calendar.getInstance();

        //Button onClickListener
        UpArrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add 1 to hours when button clicked
                hours += 1;

                //Setting text of hours
                TimerLeft.setText(timeToText(hours, 24));
            }
        });

        //Button onClickListener
        DownArrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Remove 1 to hours when button clicked
                hours -= 1;

                //Setting text of hours
                TimerLeft.setText(timeToText(hours, 24));
            }
        });

        //Button onClickListener
        UpArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add 1 to minutes when button clicked
                minutes += 1;

                //Setting text of minutes
                TimerRight.setText(timeToText(minutes, 60));
            }
        });

        //Button onClickListener
        DownArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Remove 1 to minutes when button clicked
                minutes -= 1;

                //Setting text of minutes
                TimerRight.setText(timeToText(minutes, 60));
            }
        });

        SetTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PopUp.this, CardNotification.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(PopUp.this, 0, intent, 0);
                //PendingIntent pendingIntent = PendingIntent().getBroadcast(PopUp.this, 0, intent, 0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent );
            }
        });

        //Initializing display metrics
        DisplayMetrics dm = new DisplayMetrics();

        //Setting metrics of window manager to dm
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        //Set height with display metric
        int height = dm.heightPixels;

        //Set width with display metric
        int width = dm.widthPixels;

        //Setting layout to 60% width and 40% height
        getWindow().setLayout((int) (width*.6), (int) (height*.4));

    }

    //Function to set text to 24 hour format
    public String timeToText(int time, int limit)
    {
        //initializing timer
        String timer;

        //Adding "0" to values less than 10
        //then returns timer
        if (time < 10 && time >= 0)
        {
            timer = "0" + time;
            return timer;
        }

        //Disables timer to go into negative
        //then return timer
        else if (time < 0)
        {
            timer = "00";
            return timer;
        }

        //Ensures timer does not go over limits
        //Not more than 23 for hours
        //Not more than 59 minutes
        if (time >= limit)
        {
            //Removes 1 from timer to ensure it does not breach limit
            timer = String.valueOf(limit-1);
            return timer;
        }

        //Turns timer from int into string
        //then returns timer
        timer = String.valueOf(time);
        return timer;
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = "CardNotificationChannel";
            String description = "Channel for card reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyCard", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



}
