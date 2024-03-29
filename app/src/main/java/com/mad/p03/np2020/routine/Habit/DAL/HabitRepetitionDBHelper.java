package com.mad.p03.np2020.routine.Habit.DAL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.mad.p03.np2020.routine.DAL.DBHelper;
import com.mad.p03.np2020.routine.Habit.DAL.HabitDBHelper;
import com.mad.p03.np2020.routine.Habit.Interface.HabitDBObservable;
import com.mad.p03.np2020.routine.Habit.Interface.HabitDBObserver;
import com.mad.p03.np2020.routine.background.HabitRepetitionWorker;
import com.mad.p03.np2020.routine.Habit.models.Habit;
import com.mad.p03.np2020.routine.Habit.models.HabitRepetition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 * Model used to manage the habitRepetition in SQLiteDatabase
 *
 * @author Hou Man
 * @since 20-07-2020
 */

public class HabitRepetitionDBHelper extends DBHelper implements HabitDBObservable {

    private final String TAG = "HabitRepetitionDatabase";
    private HabitDBHelper habitDBHelper;
    private Context context;
    ArrayList<HabitDBObserver> observerArrayList = new ArrayList<>();

    /**
     *
     * This method is a constructor of HabitDBHelper.
     *
     * @param context This parameter is to get the application context.
     * */
    public HabitRepetitionDBHelper(@Nullable Context context) {
        super(context);
        habitDBHelper = new HabitDBHelper(context);
        this.context = context;
    }

    /**
     *
     * This method is used to initialise the database.
     *
     * @param db This parameter is to get the SQLiteDatabase.
     * */
    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        observerArrayList = new ArrayList<>();
    }

    /**
     *
     * This method is used to upgrade the database.
     *
     * @param db This parameter is to get the SQLiteDatabase.
     *
     * @param oldVersion This parameter is the old version.
     *
     * @param newVersion This parameter is the new version.
     * */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }

    /**
     *
     * This method is used to downgrade the database.
     *
     * @param db This parameter is to get the SQLiteDatabase.
     *
     * @param oldVersion This parameter is the old version.
     *
     * @param newVersion This parameter is the new version.
     * */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    /**
     *
     * This method is used to insert the habitRepetition to the habitRepetition column in the SQLiteDatabase.
     *
     * @param habit This parameter is to get the habit object.
     *
     * @return long This will return the id for the habitRepetition after the habitRepetition is inserted to the habitRepetition column.
     * */
    public long insertHabitRepetition(Habit habit, int habitCount) {

        Log.d(TAG, "insertHabitRepetitions: "+ habit.getTitle());

        // insert the values
        ContentValues values = new ContentValues();
        values.put(HabitRepetition.COLUMN_ID, getLastAssignedRowID());
        values.put(HabitRepetition.COLUMN_HABIT_ID, habit.getHabitID());
        values.put(HabitRepetition.COLUMN_HABIT_TIMESTAMP, getTodayTimestamp());
        values.put(HabitRepetition.COLUMN_HABIT_COUNT, habitCount);
        values.put(HabitRepetition.COLUMN_HABIT_CONCOUNT, 0);
        switch (habit.getPeriod()){
            case 1:
                values.putNull(HabitRepetition.COLUMN_HABIT_CYCLE);
                values.put(HabitRepetition.COLUMN_HABIT_CYCLE_DAY, 1);
                break;

            case 7:

            case 30:
                values.put(HabitRepetition.COLUMN_HABIT_CYCLE, 1);
                values.put(HabitRepetition.COLUMN_HABIT_CYCLE_DAY, 1);
                break;

        }

        // get the writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // insert the habit
        long id =  db.insert(HabitRepetition.TABLE_NAME, null, values);
        if (id == -1){ // if id is equal to 1, there is error inserting the habit
            Log.d(TAG, "Habit: insertHabitRepetitions: " + "Error");
        }else{ // if id is not equal to 1, there is no error inserting the habit
            Log.d(TAG, "Habit: insertHabitRepetitions: " + "Successful");

        }
        // close the database
        db.close();

        return id;
    }

    /**
     *
     * This method is to get today timestamp.
     *
     * @return long This will return today timestamp in millisecond
     *
     * */
    public long getTodayTimestamp(){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date  = cal.get(Calendar.DATE);
        cal.clear();
        cal.set(year, month, date);

        return cal.getTimeInMillis();
    }

    /**
     *
     * This method is used to update the count of the habit in the SQLiteDatabase.
     *
     * @param habit This parameter is to get the habit object.
     *
     * */
    public void updateCount(Habit habit){

        HabitRepetition hr = getTodayHabitRepetitionByID(habit.getHabitID());
        int count = habit.getCount() - hr.getConCount();
        // get the readable database
        SQLiteDatabase db = this.getWritableDatabase();
        // the query of updating the row
        String query =
                "UPDATE " + HabitRepetition.TABLE_NAME +
                        " SET " + HabitRepetition.COLUMN_HABIT_COUNT +"=" + count +
                        " WHERE " + HabitRepetition.COLUMN_HABIT_ID + "=" + habit.getHabitID() +
                        " AND " + HabitRepetition.COLUMN_HABIT_TIMESTAMP + "=" + getTodayTimestamp();

        db.execSQL(query); // execute the query
        db.close(); // close the db connection
    }

    /**
     *
     * This method is to repeat the habit based on its period.
     *
     * */
    public void repeatingHabit(){
        Log.d(TAG, "repeatingHabit: ");
        // get the readable database
        SQLiteDatabase db = this.getReadableDatabase();

        long todayTimeStamp = getTodayTimestamp();
        String query = "select *,max(timestamp) from " + HabitRepetition.TABLE_NAME + " GROUP BY " + HabitRepetition.COLUMN_HABIT_ID;
        // run the query
        Cursor res =  db.rawQuery( query, null );
        if (res.getCount() <= 0){
            return;
        }
        res.moveToFirst(); // move to the first result found

        while(!res.isAfterLast()){
            long id = res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_ID));
            int cycle = res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CYCLE));
            int day = res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CYCLE_DAY));
            long currTimeStamp = res.getLong(res.getColumnIndex("max(timestamp)"));
            if (habitDBHelper.isHabitIDExisted(id)){
                Habit habit = habitDBHelper.getHabitByID(id);
                while (currTimeStamp < todayTimeStamp){
                    boolean isUpdated = false;
                    long timestamp = currTimeStamp + 86400000;
                    switch (habit.getPeriod()){
                        case 1:
                            Log.d(TAG, "repeatingHabit: DAILY");
                            if (!checkRepetitionExist(id, timestamp)){
                                insertNewRepetitionHabit(id, -1, ++day, 0, timestamp);
                                isUpdated = true;
                            }
                            currTimeStamp += 86400000;
                            break;

                        case 7:
                            Log.d(TAG, "repeatingHabit: WEEKLY");
                            if (!checkRepetitionExist(id, timestamp)){
                                if (day == 7){
                                    insertNewRepetitionHabit(id, ++cycle, 1, 0, timestamp);
                                    isUpdated = true;
                                    day = 1;

                                }else{
                                    int count = getLastDayTotalCount(id, timestamp);
                                    insertNewRepetitionHabit(id, cycle, ++day, count, timestamp);
                                    isUpdated = true;
                                }
                            }
                            currTimeStamp += 86400000;
                            break;

                        case 30:
                            Log.d(TAG, "repeatingHabit: MONTHLY");
                            if (!checkRepetitionExist(id, timestamp)){
                                if (day == 30){
                                    insertNewRepetitionHabit(id, ++cycle, 1, 0, timestamp);
                                    isUpdated = true;
                                    day = 1;
                                }
                                else{
                                    int count = getLastDayTotalCount(id, timestamp);
                                    insertNewRepetitionHabit(id, cycle, ++day, count, timestamp);
                                    isUpdated = true;
                                }
                            }
                            currTimeStamp += 86400000;
                            break;

                    }
                    if (isUpdated){
                        // write to firebase when there is changed in sql
                        Log.d(TAG, "repeatingHabit: "+habit.getHabitID()+ " AT " + timestamp );
                        HabitRepetition habitRepetition = getHabitRepetitionByTimeStamp(habit.getHabitID(), timestamp);
                        writeHabitRepetition_Firebase(habitRepetition, FirebaseAuth.getInstance().getCurrentUser().getUid());
                    }
                }

            }

            res.moveToNext(); // move to the next result

        }
        db.close();
    }

    /**
     *
     * This method is to insert new habit repetition to SQLiteDatabase
     *
     * @param habitID This is to parse the habitID
     * @param cycle This is to parse the cycle
     * @param cycle_day This is to parse the cycle day
     * @param conCount This is to parse the consecutive count
     * @param timestamp This is to parse the timestamp
     * */
    public void insertNewRepetitionHabit(long habitID, int cycle, int cycle_day, int conCount, long timestamp){
        // insert the values
        ContentValues values = new ContentValues();
        values.put(HabitRepetition.COLUMN_HABIT_ID, habitID);
        values.put(HabitRepetition.COLUMN_HABIT_TIMESTAMP, timestamp);
        values.put(HabitRepetition.COLUMN_HABIT_COUNT, 0);
        values.put(HabitRepetition.COLUMN_HABIT_CONCOUNT, conCount);
        if (cycle == -1){
            values.putNull(HabitRepetition.COLUMN_HABIT_CYCLE);
        }else{
            values.put(HabitRepetition.COLUMN_HABIT_CYCLE, cycle);
        }

        values.put(HabitRepetition.COLUMN_HABIT_CYCLE_DAY, cycle_day);

        // get the writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // insert the habit
        long id =  db.insert(HabitRepetition.TABLE_NAME, null, values);
        if (id == -1){ // if id is equal to 1, there is error inserting the habit
            Log.d(TAG, "Habit: insertNewRepetitionHabit: " + "Error");
        }else{ // if id is not equal to 1, there is no error inserting the habit
            Log.d(TAG, "Habit: insertNewRepetitionHabit: " + "Successful");

        }
        // close the database
        db.close();
    }

    /**
     *
     * This method is to get today habitRepetition by its id
     *
     * @param id This is to parse the id
     *
     * @return HabitRepetition This will return the habitRepetition object found in SQLiteDatabase.
     * */
    public HabitRepetition getTodayHabitRepetitionByID(long id){
        HabitRepetition hr = new HabitRepetition();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_HABIT_ID + " = " + id + " AND " + HabitRepetition.COLUMN_HABIT_TIMESTAMP + "=" + getTodayTimestamp();
        Cursor res =  db.rawQuery( query, null );
        if (res.getCount() > 0){
            res.moveToFirst();
            hr.setRow_id(res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_ID)));
            hr.setHabitID(id);
            hr.setTimestamp(res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_TIMESTAMP)));
            hr.setCycle(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CYCLE)));
            hr.setCycle_day(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CYCLE_DAY)));
            hr.setCount(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_COUNT)));
            hr.setConCount(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CONCOUNT)));
        }

        db.close();

        return hr;

    }

    /**
     *
     * This method is to get habitRepetition by its id and timestamp
     *
     * @param id This is to parse the id
     *
     * @param timestamp This is to parse the timestamp
     *
     * @return HabitRepetition This will return the habitRepetition object found in SQLiteDatabase.
     * */
    public HabitRepetition getHabitRepetitionByTimeStamp(long id, long timestamp){
        HabitRepetition hr = new HabitRepetition();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_HABIT_ID + " = " + id + " AND " + HabitRepetition.COLUMN_HABIT_TIMESTAMP + "=" + timestamp;
        Cursor res =  db.rawQuery( query, null );
        if (res.getCount() > 0){
            res.moveToFirst();
            hr.setRow_id(res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_ID)));
            hr.setHabitID(id);
            hr.setTimestamp(res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_TIMESTAMP)));
            hr.setCycle(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CYCLE)));
            hr.setCycle_day(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CYCLE_DAY)));
            hr.setCount(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_COUNT)));
            hr.setConCount(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CONCOUNT)));
        }

        db.close();

        return hr;
    }
    
    /**
     *
     * This method is used to send the work request
     *  to the HabitWorker(WorkManager) to do the writing firebase action
     *  when the network is connected.
     *
     * @param habitRepetition This parameter is used to get the habit object
     *
     * @param UID This parameter is used to get the userID
     *
     * */
    public void writeHabitRepetition_Firebase(HabitRepetition habitRepetition, String UID){
        Log.i(TAG, "Uploading to Firebase");

        // set constraint that the network must be connected
        Constraints myConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // put data in a data builder
        Data firebaseUserData = new Data.Builder()
                .putString("ID", UID)
                .putString("habitRepetition", habitRepetition_serializeToJson(habitRepetition))
                .build();

        // send a work request
        OneTimeWorkRequest mywork =
                new OneTimeWorkRequest.Builder(HabitRepetitionWorker.class)
                        .setConstraints(myConstraints)
                        .setInputData(firebaseUserData)
                        .build();

        WorkManager.getInstance(context).enqueue(mywork);
    }

    /**
     *
     * This method is used to serialize a single object. (into Json String)
     *
     * @param habitRepetition This parameter is used to get the habitRepetition object
     *
     * @return String This returns the serialized object.
     *
     * */
    public String habitRepetition_serializeToJson(HabitRepetition habitRepetition) {
        Gson gson = new Gson();
        Log.i(TAG,"Object serialize");
        return gson.toJson(habitRepetition);
    }

    /**This method is used to delete the all habit object in the SQLiteDatabase.* */
    public void deleteAllHabitRepetitions(){
        Log.d(TAG, "Habit: deleteAllHabitRepetitions: ");

        // get the writeable database
        SQLiteDatabase db = this.getWritableDatabase();

        // delete the habit table
        db.delete(HabitRepetition.TABLE_NAME,null,null);

        db.close(); //close the db connection
    }

    /**
     *
     * This method is used to insert the habitRepetition to the habitRepetition table in the SQLiteDatabase from firebase.
     *
     * @param hr This parameter is to get the habitRepetition object.
     *
     * @param UID This parameter is the get the UID to refer which habit column is going to be inserted.
     *
     * */
    public void insertHabitRepetitionFromFirebase(HabitRepetition hr, String UID) {

        Log.d(TAG, "insertHabitFromFirebase: "+UID);

        // insert the values
        ContentValues values = new ContentValues();
        values.put(HabitRepetition.COLUMN_ID, hr.getRow_id());
        values.put(HabitRepetition.COLUMN_HABIT_ID, hr.getHabitID());
        values.put(HabitRepetition.COLUMN_HABIT_TIMESTAMP, hr.getTimestamp());
        values.put(HabitRepetition.COLUMN_HABIT_COUNT, hr.getCount());
        values.put(HabitRepetition.COLUMN_HABIT_CONCOUNT, hr.getConCount());
        int cycle = hr.getCycle();
        if (cycle == 0){
            values.putNull(HabitRepetition.COLUMN_HABIT_CYCLE);
        }else{
            values.put(HabitRepetition.COLUMN_HABIT_CYCLE, cycle);
        }
        values.put(HabitRepetition.COLUMN_HABIT_CYCLE_DAY, hr.getCycle_day());

        // get the writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // insert the habit
        long id =  db.insert(HabitRepetition.TABLE_NAME, null, values);
        if (id == -1){ // if id is equal to 1, there is error inserting the habit
            Log.d(TAG, "HabitRepetition: insertHabitRepetition: " + "Error");
        }else{ // if id is not equal to 1, there is no error inserting the habit
            Log.d(TAG, "HabitRepetition: insertHabitRepetition: " + "Successful");

        }
        // close the database
        db.close();
        notifyDbChanged();

    }

    /**
     *
     * This method is to get last assigned row id in habitRepetition table.
     *
     * @return long this will return the last assigned row id.
     * */
    public long getLastAssignedRowID(){
        SQLiteDatabase db = this.getReadableDatabase();
        long id = 1;
        Cursor res =  db.rawQuery( "select max(_id) from " + HabitRepetition.TABLE_NAME , null );
        if (res.getCount() > 0){
            res.moveToFirst(); //Only getting the first value
            id = res.getLong(res.getColumnIndex("max(_id)")) + 1;
        }

        db.close();

        return id;
    }

    /**
     *
     * This method is used to delete the habitRepetition object in the SQLiteDatabase.
     *
     * @param habit This parameter is to get the habit object.
     *
     * */
    public void deleteHabitRepetition(Habit habit){
        Log.d(TAG, "Habit: deleteHabit: ");

        // get the writeable database
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = HabitRepetition.COLUMN_HABIT_ID + "=?"; // specify to delete based on the column id

        // put the column id
        String[] whereArgs = new String[] { String.valueOf(habit.getHabitID()) };

        // delete the habit column
        db.delete(HabitRepetition.TABLE_NAME, whereClause, whereArgs);

        db.close(); // close the db connection
    }

    /**
     *
     * This method is to get all habitRepetitionsID by the habitID
     *
     * @param habitID This is to parse the habitID
     *
     * @return ArrayList<Long> this will return the habitRepetitionsID in long array list
     * */
    public ArrayList<Long> getAllHabitRepetitionsIDByHabitID(long habitID) {
        ArrayList<Long> arr = new ArrayList<>();

        // get the readable database
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select * from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_HABIT_ID + " = " + habitID;
        Log.d(TAG, "getAllHabitRepetitionsIDByHabitID: "+query);
        // run the query
        Cursor res = db.rawQuery(query, null);

        if (res.getCount() > 0) {
            Log.d(TAG, "getAllHabitRepetitionsIDByHabitIDCount: "+res.getCount());
            res.moveToFirst(); // move to the first result found

            while (!res.isAfterLast()) {
                long id = res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_ID));
                Log.d(TAG, "getAllHabitRepetitionsIDByHabitID: "+id);
                arr.add(id);
                res.moveToNext();
            }
        }

        db.close();
        return arr;
    }

    /**
     *
     * This method is to get all habitRepetitions by the habitID
     *
     * @param habitID This is to parse the habitID
     *
     * @return ArrayList<HabitRepetition> this will return the habitRepetitions in array list
     * */
    public ArrayList<HabitRepetition> getAllHabitRepetitionsByHabitID(long habitID) {
        ArrayList<HabitRepetition> arr = new ArrayList<>();

        // get the readable database
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select * from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_HABIT_ID + " = " + habitID;
        // run the query
        Cursor res = db.rawQuery(query, null);

        if (res.getCount() > 0) {
            Log.d(TAG, "getAllHabitRepetitionsByHabitID: ");
            res.moveToFirst(); // move to the first result found

            while (!res.isAfterLast()) {
                HabitRepetition hr = new HabitRepetition();
                hr.setRow_id(res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_ID)));
                hr.setHabitID(res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_ID)));
                hr.setCycle(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CYCLE)));
                hr.setCycle_day(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CYCLE_DAY)));
                hr.setTimestamp(res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_TIMESTAMP)));
                hr.setCount(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_COUNT)));
                hr.setConCount(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CONCOUNT)));
                arr.add(hr);

                res.moveToNext();
            }
        }

        db.close();
        return arr;
    }

    /**
     *
     * This method is to get the max cycle by the habitID
     *
     * @param habitID This is to parse the habitID
     *
     * @return int This will return the max cycle for the habit
     * */
    public int getMaxCycle(long habitID){
        int cycle = 0;
        // get the readable database
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select max(cycle) from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_HABIT_ID + " = " + habitID;
        // run the query
        Cursor res = db.rawQuery(query, null);

        if (res.getCount() > 0) {
            Log.d(TAG, "getAllHabitRepetitionsByHabitID: ");
            res.moveToFirst(); // move to the first result found

            cycle = res.getInt(res.getColumnIndex("max(cycle)"));

        }

        db.close();

        return cycle;
    }

    /**
     *
     * This method is to get the max count for specific cycle by the habitID
     *
     * @param habitID This is to parse the habitID
     *
     * @param cycle This is to parse the cycle
     *
     * @return int This will return the max count for specific cycle of the habit
     * */
    public int getMaxCountByCycle(long habitID, int cycle){
        int count = 0;
        // get the readable database
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select max(count+conCount) from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_HABIT_ID + " = " + habitID + " AND " + HabitRepetition.COLUMN_HABIT_CYCLE + " = " + cycle;
        // run the query
        Cursor res = db.rawQuery(query, null);

        if (res.getCount() > 0) {
            Log.d(TAG, "getAllHabitRepetitionsByHabitID: ");
            res.moveToFirst(); // move to the first result found

            count = res.getInt(res.getColumnIndex("max(count+conCount)"));

        }

        db.close();

        return count;
    }

    /**
     *
     * This method is to check whether there is next month data for the habit
     *
     * @param habitID This is to parse the habitID
     *
     * @param next_ms This is to parse the next month ms
     *
     * @return boolean This will return the boolean value to indicate whether there is next month data for the habit
     * */
    public boolean isNextMonth(long habitID, long next_ms){
        boolean isNextMonth = false;
        // get the readable database
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select count(*) from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_HABIT_ID + " = " + habitID + " AND " + HabitRepetition.COLUMN_HABIT_TIMESTAMP + " >= " + next_ms;
        // run the query
        Cursor res = db.rawQuery(query, null);
        if (res.getCount() > 0) {
            res.moveToFirst(); // move to the first result found

            int count = res.getInt(res.getColumnIndex("count(*)"));
            if (count > 0){
                isNextMonth = true;
            }
        }

        db.close();

        return isNextMonth;
    }

    /**
     *
     * This method is to get count between months
     *
     * @param habitID This is to parse the habitID
     *
     * @param ms This is to parse the month ms
     *
     * @param next_ms This is to parse the next month ms
     *
     * @return int This will return count between months
     * */
    public int getCountBetweenMonth(long habitID, long ms, long next_ms){
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select sum(count) from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_HABIT_ID + " = " + habitID + " AND (" + HabitRepetition.COLUMN_HABIT_TIMESTAMP + " >= " + ms + " AND  " + HabitRepetition.COLUMN_HABIT_TIMESTAMP + " < " + next_ms + ")";
        // run the query
        Cursor res = db.rawQuery(query, null);

        if (res.getCount() > 0) {
            res.moveToFirst(); // move to the first result found
            count = res.getInt(res.getColumnIndex("sum(count)"));
        }

        db.close();

        return count;
    }

    /**
     *
     * This method is to check whether the repetition exist
     *
     * @param habitID This is to parse the habitID
     *
     * @param timestamp This is to get the timestamp
     *
     * @return boolean This will return the boolean value which indicates whether the repetition exist
     * */
    public boolean checkRepetitionExist(long habitID, long timestamp){
        boolean isExisted = false;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =  db.rawQuery( "select * from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_HABIT_ID + " = " + habitID + " AND " + HabitRepetition.COLUMN_HABIT_TIMESTAMP + "=" + timestamp, null );
        if (cursor.getCount() > 0){
            isExisted = true;
        }

        db.close();
        Log.d(TAG, "checkTodayRepetition: " + isExisted);
        return isExisted;
    }

    /**
     *
     * This method is to get the last day total count
     *
     * @param habitID This is to parse the habitID
     *
     * @param timestamp This is to get the timestamp
     *
     * @return int This will return the last day total count
     * */
    public int getLastDayTotalCount(long habitID, long timestamp){
        int total = 0;
        timestamp -= 86400000;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select * from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_HABIT_ID + " = " + habitID + " AND " + HabitRepetition.COLUMN_HABIT_TIMESTAMP + " = " + timestamp;
        // run the query
        Cursor res = db.rawQuery(query, null);
        if (res.getCount() > 0) {
            res.moveToFirst(); // move to the first result found

            int count = res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_COUNT));
            int conCount = res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CONCOUNT));
            total = count + conCount;
        }

        db.close();

        return total;

    }

    /**
     *
     * This method is to check whether the repetition exist by its row id
     *
     * @param rowID This is to parse the row id
     *
     * @return boolean This will return the boolean value which indicates whether the repetition exist
     * */
    public boolean isHabitRepetitionExisted(long rowID){
        boolean isExisted = false;

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select * from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_ID + " = " + rowID;

        Cursor res =  db.rawQuery( query  , null );
        if (res.getCount() > 0){
            isExisted = true;
        }

        db.close();

        return isExisted;
    }

    /**
     *
     * This method is to get the habitRepetition by its row id
     *
     * @param id This is to get the row id
     *
     * @return HabitRepetition This will return the habitRepetition object by its row id
     * */
    public HabitRepetition getHabitRepetitionByRowID(long id){
        HabitRepetition hr = new HabitRepetition();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + HabitRepetition.TABLE_NAME + " WHERE " + HabitRepetition.COLUMN_ID + " = " + id;
        Cursor res =  db.rawQuery( query, null );
        if (res.getCount() > 0){
            res.moveToFirst();
            hr.setRow_id(res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_ID)));
            hr.setHabitID(res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_ID)));
            hr.setTimestamp(res.getLong(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_TIMESTAMP)));
            hr.setCycle(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CYCLE)));
            hr.setCycle_day(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CYCLE_DAY)));
            hr.setCount(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_COUNT)));
            hr.setConCount(res.getInt(res.getColumnIndex(HabitRepetition.COLUMN_HABIT_CONCOUNT)));
        }

        db.close();

        return hr;
    }

    /**
     *
     * This method is to update the habitRepetition row
     *
     * @param hr This is to get the HabitRepetition object
     *
     * */
    public void update(HabitRepetition hr){
        String id_filter = HabitRepetition.COLUMN_ID + " = " +hr.getRow_id();

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //update row id
        values.put(HabitRepetition.COLUMN_HABIT_ID, hr.getHabitID());
        values.put(HabitRepetition.COLUMN_HABIT_TIMESTAMP, hr.getTimestamp());
        values.put(HabitRepetition.COLUMN_HABIT_COUNT, hr.getCount());
        values.put(HabitRepetition.COLUMN_HABIT_CONCOUNT, hr.getConCount());
        values.put(HabitRepetition.COLUMN_HABIT_CYCLE, hr.getCycle());
        values.put(HabitRepetition.COLUMN_HABIT_CYCLE_DAY, hr.getCycle_day());

        // update the habit column
        db.update(HabitRepetition.TABLE_NAME, values, id_filter, null);
        db.close(); // close the db connection
        notifyDbChanged();
    }

    /**
     *
     * This method is to remove the habitRepetition row
     *
     * @param hr This is to get the HabitRepetition object
     *
     * */
    public void removeOneData(HabitRepetition hr) {

        // Find database that match the row data. If it found, delete and return true

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(
                HabitRepetition.TABLE_NAME,  // The table to delete from
                HabitRepetition.COLUMN_ID + " = ?", //The condition
                new String[]{String.valueOf(hr.getRow_id())} // The args will be replaced by ?
        );

        Log.d(TAG, "removeOneData: "+hr.getRow_id());
        db.close();
        notifyDbChanged();

    }

    /**
     *
     * This method is to repeat the specific habit
     *
     * @param habitID This is to get the habitID
     *
     * */
    public void repeatingSpecificHabitByID(long habitID){
        long todayTimeStamp = getTodayTimestamp();
        Habit habit = habitDBHelper.getHabitByID(habitID);
        long id = habit.getHabitID();
        int cycle = (habit.getPeriod() == 1) ? 0 : 1;
        int day = 0;
        long currTimeStamp = getTimestamp(habit.getTime_created());
        if (currTimeStamp == 0){
            return;
        }
        Log.d(TAG, "repeatingSpecificHabitByID: "+currTimeStamp);

        if (habitDBHelper.isHabitIDExisted(id)) {
            while (currTimeStamp <= todayTimeStamp) {
                boolean isUpdated = false;
                long timestamp = currTimeStamp;
                switch (habit.getPeriod()) {
                    case 1:
                        Log.d(TAG, "repeatingHabit: DAILY");
                        if (!checkRepetitionExist(id, timestamp)) {
                            insertNewRepetitionHabit(id, -1, ++day, 0, timestamp);
                            isUpdated = true;
                        }
                        currTimeStamp += 86400000;
                        break;

                    case 7:
                        Log.d(TAG, "repeatingHabit: WEEKLY");
                        if (!checkRepetitionExist(id, timestamp)) {
                            if (day == 7) {
                                insertNewRepetitionHabit(id, ++cycle, 1, 0, timestamp);
                                isUpdated = true;
                                day = 1;

                            } else {
                                int count = getLastDayTotalCount(id, timestamp);
                                insertNewRepetitionHabit(id, cycle, ++day, count, timestamp);
                                isUpdated = true;
                            }
                        }
                        currTimeStamp += 86400000;
                        break;

                    case 30:
                        Log.d(TAG, "repeatingHabit: MONTHLY");
                        if (!checkRepetitionExist(id, timestamp)) {
                            if (day == 30) {
                                insertNewRepetitionHabit(id, ++cycle, 1, 0, timestamp);
                                isUpdated = true;
                                day = 1;
                            } else {
                                int count = getLastDayTotalCount(id, timestamp);
                                insertNewRepetitionHabit(id, cycle, ++day, count, timestamp);
                                isUpdated = true;
                            }
                        }
                        currTimeStamp += 86400000;
                        break;

                }
                if (isUpdated) {
                    Log.d(TAG, "repeatingHabit: " + habit.getHabitID() + " AT " + timestamp);
                    HabitRepetition habitRepetition = getHabitRepetitionByTimeStamp(habit.getHabitID(), timestamp);
                    writeHabitRepetition_Firebase(habitRepetition, FirebaseAuth.getInstance().getCurrentUser().getUid());
                }
            }
        }
    }

    /**
     *
     * This method is to get the timestamp ms from dateString
     *
     * @param time_created This is to get the dateString
     *
     * @return long This will return the timestamp in millisecond
     *
     * */
    public long getTimestamp(String time_created) {
        long millis = 0;
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            Date date = sdf.parse(time_created);
            millis = date.getTime();
        }catch(ParseException e){
            e.printStackTrace();
        }
        return millis;
    }

    /**
     *
     * This method is to register the habitDBObserver in the observerArrayList
     *
     * */
    @Override
    public void registerDbObserver(HabitDBObserver habitDBObserver) {
        if (!observerArrayList.contains(habitDBObserver)){
            observerArrayList.add(habitDBObserver);
        }
    }

    /**
     *
     * This method is to remove the habitDBObserver in the observerArrayList
     *
     * */
    @Override
    public void removeDbObserver(HabitDBObserver habitDBObserver) {
        observerArrayList.remove(habitDBObserver);
    }

    /**
     *
     * This method is to notify the backend when there is changes is sql triggered by firebase.
     *
     * */
    @Override
    public void notifyDbChanged() {
        for (HabitDBObserver habitDBObserver :observerArrayList){
            if (habitDBObserver != null){
                habitDBObserver.onDatabaseChanged();
                Log.v(TAG,"SQLiteDatabase onChanged triggered");
            }}
    }


}
