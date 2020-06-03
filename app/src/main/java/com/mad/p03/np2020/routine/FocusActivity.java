package com.mad.p03.np2020.routine;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.mad.p03.np2020.routine.Class.Focus;
import com.mad.p03.np2020.routine.background.FocusWorker;
import com.mad.p03.np2020.routine.database.FocusDBHelper;
import com.mad.p03.np2020.routine.Class.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.lang.String.*;


/**
 *
 * Model used to manage the section
 *
 * @author Lee Quan Sheng
 * @since 02-06-2020
 */


public class FocusActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener, HistoryFragment.OnFragmentInteractionListener, View.OnLongClickListener, View.OnTouchListener {


    /**Timer widget*/
    /**Button for timer*/
    private Button focusButton;

    /**Timer for minutes and seconds*/
    private int tmins, tsecs = 0;

    /**This button state is used to track the timer button next state*/
    private String BUTTON_STATE = "EnterTask";

    /**This button is to submit the task that user key*/
    private ImageButton taskSubmit; //

    /**EditText for User to enter in the task*/
    private EditText taskInput; //

    /**TextView on the display of the timer*/
    private TextView min, sec, semicolon, textDisplay;

    /**Used to track the timer left for Focus*/
    private long mTimeLeftInMillis = 0;

    /**Button to control the timer*/
    private ImageView minup, mindown, secup, secdown, mface;

    /**Button used for event control the timer*/
    private boolean bupmin, bdownmin, bupsec, bdownsec;

    private Handler repeatUpdateHandler = new Handler(); //For long touch view
    private CountDownTimer mCountDownTimer; //Main Counteractive for Focus
    private CountDownTimer eCountDownTimer; //Auto closed for Focus

    //History Widgets
    private String dateTimeTask, currentTask, mCompletion;
    private final String TAG = "Focus";

    /**Notification channel ID is set to channel 1*/
    private static final String CHANNEL_1_ID = "channel1";

    final String title = "You have an ongoing Focus";
    final String message = "Come back now before your Sun becomes depressed!";

    //Firebase
    private DatabaseReference myRef;

    //User
    private User user = new User();

    //Local Database
    FocusDBHelper focusDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Animation translateAnimation = AnimationUtils.loadAnimation(this, R.anim.translate_anims);
        initialization(); //Process of data

        //ImageButton
        ImageButton imageButton = findViewById(R.id.history);
        focusButton = findViewById(R.id.start);
        taskSubmit = findViewById(R.id.taskSubmit);

        //ImageView
        minup = findViewById(R.id.minUp);
        mindown = findViewById(R.id.minDown);
        secup = findViewById(R.id.secUp);
        secdown = findViewById(R.id.secDown);
        mface = findViewById(R.id.assistant);

        //TextView
        min = findViewById(R.id.mins);
        sec = findViewById(R.id.secs);
        semicolon = findViewById(R.id.semicolon);
        textDisplay = findViewById(R.id.diplayText);
        taskInput = findViewById(R.id.taskInput);

        //SetOnclickListener
        imageButton.setOnClickListener(this);
        minup.setOnClickListener(this);
        secdown.setOnClickListener(this);
        secup.setOnClickListener(this);
        mindown.setOnClickListener(this);
        focusButton.setOnClickListener(this);
        taskSubmit.setOnClickListener(this);

        //SetLongClickListener
        minup.setOnLongClickListener(this);
        secdown.setOnLongClickListener(this);
        secup.setOnLongClickListener(this);
        mindown.setOnLongClickListener(this);

        //SetLongClickListener
        minup.setOnTouchListener(this);
        secdown.setOnTouchListener(this);
        secup.setOnTouchListener(this);
        mindown.setOnTouchListener(this);

        taskInput.setOnFocusChangeListener(this);

        mface.startAnimation(translateAnimation);
    }

    /**
     *
     * Initialization the focusActivity
     *
     * Initialize minutes, seconds back to zero
     * Get Firebase Data
     * Get Local Database Data
     * Initialize object
     */
    private void initialization() {
        Log.v(TAG, "Database does not exist");
        focusDBHelper = new FocusDBHelper(FocusActivity.this);
        if(focusDBHelper.isTableExists("FOCUS_TABLE")){
            Log.v(TAG, "Database Exist");
            focusDBHelper = new FocusDBHelper(FocusActivity.this);
            user.setmFocusList(focusDBHelper.getAllData());
            FirebaseDatabase();
        }else{
            Log.v(TAG, "Database does not exist");
            focusDBHelper = new FocusDBHelper(FocusActivity.this);
            FirebaseDatabase();
            user.readFocusFirebase(this);
        }

        tmins = 0;
        tsecs = 0;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        //Add it to LocalDatabase List
        Log.v(TAG, "Local database: " + focusDBHelper.getAllData().toString());
    }

    //
    //This function is to track the button state so that it can show its respective view
    /**
     * Type of button state: Running, Fail, Success, Reset
     * @Param BUTTON_STATE Running, Fail, Success Reset
     *
     * This is used to trace the button state to display the respective view
     */
    private void focusTime() {
        switch (BUTTON_STATE) {
            case "EnterTask": //Enter Task view where user can enter its view
                Log.v(TAG, "Button Enter Task is clicked");
                if (tsecs == 0 && tmins == 0) {
                    textDisplay.setText(R.string.FAIL_TIMER);
                    textDisplay.setTextColor(ContextCompat.getColor(this, R.color.chineseRed));
                } else {
                    textDisplay.setTextColor(ContextCompat.getColor(this, R.color.black));
                    ShowKeyboard();
                    taskInput.setText("");
                }
                break;

            case "Reset": //Reset view where the view will become its initiate state
                Log.v(TAG, "Button Reset Task is clicked");
                textDisplay.setText(R.string.REST_STATUS);
                timerReset();
                BUTTON_STATE = "EnterTask";
                break;

            case "Running":
                Log.v(TAG, "Button Running Task is clicked");
                textDisplay.setText(R.string.PROCESS_STATUS);
                timeRunner();
                long totaltime = (tmins * 60) + tsecs;
                long millisInput = totaltime * 1000;
                Log.v(TAG, valueOf(millisInput));
                BUTTON_STATE = "Fail";
                StartTimer(millisInput);
                break;

            case "Success":
                Log.v(TAG, "Button Sucess Task is clicked");
                textDisplay.setText(R.string.SUCCESS_STATUS);
                timerSuccess();
                BUTTON_STATE = "Reset";
                mCompletion = "True";
                String dateSuccess = new SimpleDateFormat("ddMMyyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                Focus focusViewHolder = new Focus(dateTimeTask, tmins + ":" + tsecs, currentTask, mCompletion);
                focusViewHolder.setFbID(dateSuccess);
                writeToDatabase(focusViewHolder);
                break;

            case "Fail":
                Log.v(TAG, "Button Fail Task is clicked");
                textDisplay.setText(R.string.FAIL_STATUS);
                timerFail();
                mCountDownTimer.cancel(); //Pause timer
                BUTTON_STATE = "Reset";
                mCompletion = "False";
                String dateFail = new SimpleDateFormat("ddMMyyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                Focus focus = new Focus(dateTimeTask, tmins + ":" + tsecs, currentTask, mCompletion);
                focus.setFbID(dateFail);
                writeToDatabase(focus);
                break;
        }
    }


    /**
     *
     *
     * Write to local Database
     *
     * @Param Focus passed in the new focus object to local database
     */
    private void writeToDatabase(Focus focus) {
        focusDBHelper.addData(focus); //Add to database
        user.addFocusList(focus); //Adding it to list
        writeDataFirebase(focus);
    }


    /**
     *
     *
     * Used for update button sequencing (timer)
     *
     */
    private void timeRunner() { //Timer running
        BUTTON_STATE = "Running";

        focusButton.setText(R.string.StopTimer);
        minup.setVisibility(View.INVISIBLE);
        mindown.setVisibility(View.INVISIBLE);
        secup.setVisibility(View.INVISIBLE);
        secdown.setVisibility(View.INVISIBLE);
        mface.setImageResource(R.drawable.ic_focus_ast_down);
    }

    /**
     *
     *
     * Update each view to a fail view
     *
     * If timer doesnt hit 0, user clicks on button_state on Give Up
     *
     */
    private void timerFail() { //Give up
        BUTTON_STATE = "Fail";

        sec.setTextColor(Color.RED);
        min.setTextColor(Color.RED);
        semicolon.setTextColor(Color.RED);
        focusButton.setText(R.string.BUTTON_FAIL);
        mface.setImageResource(R.drawable.ic_focus_ast_sad);
    }

    /**
     *
     * Update each view to a success view
     *
     * If timer hits 0, it will execute
     *
     */
    private void timerSuccess() { //Timer hits 0
        BUTTON_STATE = "Success";

        sec.setTextColor(Color.parseColor("#CAEFD1"));
        min.setTextColor(Color.parseColor("#CAEFD1"));
        semicolon.setTextColor(Color.parseColor("#CAEFD1"));
        focusButton.setText(R.string.BUTTON_RESTART);
        mface.setImageResource(R.drawable.ic_focus_ast_happy);
    }

    /**
     *
     * Reset the text of the timer
     *
     * Will be executed when the task has ended
     */
    private void timerReset() { //Resetting to state
        BUTTON_STATE = "Reset";

        sec.setTextColor(Color.BLACK);
        min.setTextColor(Color.BLACK);
        semicolon.setTextColor(Color.BLACK);
        focusButton.setText(R.string.startTimer);
        mface.setImageResource(R.drawable.focus_ast);

        minup.setVisibility(View.VISIBLE);
        mindown.setVisibility(View.VISIBLE);
        secup.setVisibility(View.VISIBLE);
        secdown.setVisibility(View.VISIBLE);

        min.setText(R.string.timer_ground);
        sec.setText(R.string.timer_ground);
        tsecs = 0;
        tmins = 0;
        focusButton.setText(R.string.BUTTON_START);
    }


    /**
     *
     *
     * Update the text of the timer
     *
     * Will be executed every click on the increment or decrement button
     */
    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        Log.v(TAG, "Counting down");
        min.setText(format(Locale.US, "%02d", minutes));
        sec.setText(format(Locale.US, "%02d", seconds));
    }

    /***
     *
     * OnClick event listener for each button on the Focus activity
     *
     * @param v Passing the current view to this content
     */
    @Override
    public void onClick(View v) {
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.history: //Open history page
                openHistory();
                Log.v(TAG, "Open History Page");
                break;
            case R.id.minUp: //Increment Minutes
                tmins = increment(tmins, "min");
                min.setText(format(Locale.US, "%02d", tmins));
                Log.v(TAG, "Increase Minutes: " + tmins);
                break;
            case R.id.minDown:  //Decrement Minutes
                tmins = decrement(tmins, "min");
                min.setText(format(Locale.US, "%02d", tmins));
                Log.v(TAG, "Decrease Minutes: " + tmins);
                break;
            case R.id.secUp:    //Increment seconds
                tsecs = increment(tsecs, "sec");
                sec.setText(format(Locale.US, "%02d", tsecs));
                Log.v(TAG, "Increase Seconds:" + tsecs);
                break;
            case R.id.secDown:  //Decrement seconds
                tsecs = decrement(tsecs, "sec");
                sec.setText(format(Locale.US, "%02d", tsecs));
                Log.v(TAG, "Decrease Seconds:" + tsecs);
                break;
            case R.id.start:
                //this button has 4 types: Start, Give up, Try Again, Restart, Enter Task
                focusTime();
                break;
            case R.id.taskSubmit:
                if (taskInput.getText().toString().isEmpty()) {
                    textDisplay.setText("Please enter a task");
                } else {
                    HideKeyboard();
                    currentTask = taskInput.getText().toString();
                    dateTimeTask = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault()).format(new Date());
                    Log.v(TAG, "Task: " + currentTask + " Date: " + dateTimeTask);
                    BUTTON_STATE = "Running";
                    taskSubmit.setVisibility(View.INVISIBLE);
                    focusTime();
                    break;
                }
        }
    }

    /**
     *
     * Event State onFocusChange
     *
     * Used detect if user click on outside of keyboard so it can automatically hide keyboard
     *
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.taskInput:
                if (!hasFocus) {
                    HideKeyboard();
                }
                break;
        }
    }

    /**
     *
     * Event State onLongClick
     *
     */
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.minUp:
                bupmin = true;
                break;
            case R.id.minDown:
                bdownmin = true;
                break;
            case R.id.secUp:
                bupsec = true;
                break;
            case R.id.secDown:
                bdownsec = true;
                break;
        }
        repeatUpdateHandler.post(new RptUpdater());
        return false;
    }

    /**
     *
     * Event State touchRelease
     *
     * The event onTouch used to stop increment/decrement timer continuously when the button is onRelease
     * @Param
     */
    public boolean touchRelease(boolean btimer, MotionEvent event) { //On release hold of timer
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL && btimer) {
            return false;
        }
        return false;
    }

    class RptUpdater implements Runnable {
        public void run() {
            if (bupmin) {
                tmins = increment(tmins, "min");
                min.setText(format(Locale.US, "%02d", tmins));
            } else if (bdownmin) {
                tmins = decrement(tmins, "min");
                min.setText(format(Locale.US, "%02d", tmins));
            } else if (bupsec) {
                tsecs = increment(tsecs, "sec");
                sec.setText(format(Locale.US, "%02d", tsecs));
            } else if (bdownsec) {
                tsecs = decrement(tsecs, "sec");
                sec.setText(format(Locale.US, "%02d", tsecs));
            }
            repeatUpdateHandler.postDelayed(new RptUpdater(), 150);
        }
    }

    /**
     *
     * Event State onTouch
     *
     * The event onTouch used to increment/decrement timer continuously when the button is onhold
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (v.getId() /*to get clicked view id**/) {
            case R.id.minUp:
                bupmin = touchRelease(bupmin, event);
                break;
            case R.id.minDown:
                bdownmin = touchRelease(bdownmin, event);
                break;
            case R.id.secUp:
                bupsec = touchRelease(bupsec, event);
                break;
            case R.id.secDown:
                bdownsec = touchRelease(bdownsec, event);
                break;
        }
        return false;
    }

    /**
     *
     * Event State onPause
     * If user exited the app, notification is pushed
     * Within 10 seconds, it will automatically count as fail
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (BUTTON_STATE.equals("Fail")) {
            createNotification(); //Notification pushed
            eCountDownTimer = new CountDownTimer(10000, 1000) { //timer countdown start
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.v(TAG, "Time left for user to entry before auto destroy: " + millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    focusButton.callOnClick(); //Simulates the button onclick to assume the failure of the task after 10 seconds
                    Log.v(TAG, "Destroyed");
                }
            }.start();
        }
    }

    /**
     *
     * Event State onResume
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (eCountDownTimer != null) {
            eCountDownTimer.cancel();
            Log.v(TAG, "Resume");
        }
    }

    /**
     *
     * Open History Fragment
     */
    public void openHistory() { //Open history tab
        HistoryFragment fragmentFocus = HistoryFragment.newInstance(user, focusDBHelper);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.fragment_container, fragmentFocus, "HISTORY FRAGMENT").commit();

    }

    /**
     *
     * Decrement time of user preferences
     *
     * @return int return back the decremented time
     * @param tChill used to passed in the current value of the time
     * @param type used to passed to the type of the time (Minutes, Hours, Seconds)
     */
    @Override
    public void onFragmentInteraction() {
        onBackPressed();
    }

    /**
     *
     * Decrement time of user preferences
     *
     * @return int return back the decremented time
     * @param tChill used to passed in the current value of the time
     * @param type used to passed to the type of the time (Minutes, Hours, Seconds)
     */
    public int decrement(int tChill, String type) { //Increment method for timer
        if (type.equals("min")) {
            if (tChill != 0) tChill--;
        } else {
            if (tChill != 0) {
                tChill -= 5;
            } else {
                tChill = 55;
            }
        }
        return tChill;
    }

    /**
     *
     * Increment time of user preferences
     *
     * @return int return back the decremented time
     * @param tChill used to passed in the current value of the time
     * @param type used to passed to the type of the time (Minutes, Hours, Seconds)
     */
    public int increment(int tChill, String type) { //Decrement method for timer
        if (type.equals("min")) {
            tChill++;
        } else {
            if (tChill != 55) {
                tChill += 5;
            } else {
                tChill = 0;
            }
        }
        return tChill;
    }

    /**
     *
     * Start Timer for Focus task activity
     * @param TimeLeftInMillis used to passed in the time set on the activity to count down the timer
     */
    private void StartTimer(long TimeLeftInMillis) {
        Log.v(TAG, "Timer Start");

        mCountDownTimer = new CountDownTimer(TimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v(TAG, "OnTick: " + millisUntilFinished);
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                Log.v(TAG, "Completed");
                BUTTON_STATE = "Success";
                focusTime();
            }
        }.start();
    }

    /**
     *
     * Create Notification
     */
    private void createNotification() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) { //API level for Kitkat
            Intent intent = new Intent(this, FocusActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(FocusActivity.this, (int) System.currentTimeMillis(), intent, 0);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification noti = new Notification.Builder(FocusActivity.this).setContentTitle(title).setContentText(message).setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pIntent).build();
            if (notificationManager != null) {
                notificationManager.notify(0, noti);
            } else {
                Log.e(TAG, "Notification manager is a nullpointer");
            }
            Log.v("Notification", "Pushed");
        } else {//API level for other than kitkat
            //Creation Channel
            NotificationChannel channel = new NotificationChannel(CHANNEL_1_ID, "Channel 1", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Focus");
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(channel);
            sendChannel1();
        }

    }

    /**
     *
     * Send notification to channel 1, used for api above 24
     */
    private void sendChannel1() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(getApplicationContext(), FocusActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID).setContentIntent(pIntent).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(message).setPriority(NotificationCompat.PRIORITY_HIGH).setCategory(NotificationCompat.CATEGORY_MESSAGE).build();
        assert notificationManager != null;
        notificationManager.notify(1, notification);

    }

    /**
     *
     * Set Reference Data from firebase based on the UID
     */
    private void FirebaseDatabase() { //Firebase Reference
        user.setUID("V30jZctVgSPh00CVskSYiXNRezC2");
        Log.i(TAG, "Getting firebase for User ID " + user.getUID());
        myRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUID());
        Log.i(TAG, "checks for myRef: " + myRef);
    }

    /**
     * Writing new Focus activity data to Google Firebase
     *
     * The OneTimeWorkRequest is used to set condition if there is network connectivity
     */
    private void writeDataFirebase(Focus focus) {
        Log.i(TAG, "Uploading to Database");

        Constraints myConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        Data firebaseUserData = new Data.Builder()
                .putString("ID", user.getUID())
                .putString("focusData", serializeToJson(focus))
                .putBoolean("deletion", false)
                .build();
        OneTimeWorkRequest mywork =
                new OneTimeWorkRequest.Builder(FocusWorker.class)
                        .setConstraints(myConstraints)
                        .setInputData(firebaseUserData)
                        .build();

        WorkManager.getInstance(this).enqueue(mywork);
    }


    /**
     * Show soft keyboard for user input task
     */
    private void ShowKeyboard() {
        taskInput.setVisibility(View.VISIBLE);
        final InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        taskSubmit.setVisibility(View.VISIBLE);
        assert mgr != null;
        taskInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                taskInput.requestFocus();
                mgr.showSoftInput(taskInput, 0);
            }
        }, 100);
        Log.i(TAG, "Show Keyboard");

    }


    /**
     * Hide soft keyboard for user input task
     */
    private void HideKeyboard() {
        taskInput.setVisibility(View.INVISIBLE);
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        taskSubmit.setVisibility(View.INVISIBLE);
        assert mgr != null;
        mgr.hideSoftInputFromWindow(taskInput.getWindowToken(), 0);
        Log.i(TAG, "Hide Keyboard");
    }

    /**
     * Serialize a single object.
     * @return String this returns the custom object class as a string
     */
    public String serializeToJson(Focus myClass) {
        Gson gson = new Gson();
        Log.i(TAG, "Object serialize");
        return gson.toJson(myClass);
    }

}
