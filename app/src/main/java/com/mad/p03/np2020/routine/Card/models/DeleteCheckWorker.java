package com.mad.p03.np2020.routine.Card.models;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.p03.np2020.routine.models.Check;
import com.mad.p03.np2020.routine.models.Section;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 *
 * This is to delete the data in firebase task node from
 * the background which implements the Worker.
 *
 * @author Jeyavishnu
 * @since 02-06-2020
 *
 */
public class DeleteCheckWorker extends Worker {

    public DeleteCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    /**
     *
     * To do background processing, to delete the task data
     * in firebase using the task id , section id and user id to
     * delete from firebase.
     *
     * Data that need to be sent over are ID, SectionID and UID.
     * You can use {@code OneTimeWorkRequest.setInputData(data)} to send and
     * {@code getInputData()} to retrieve it.
     *
     * @return Result This is tell what happen to the background work
     */
    @NonNull
    @Override
    public Result doWork() {
        final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String ID = getInputData().getString("ID") ;
        String taskID =  getInputData().getString(Check.COLUMN_TaskID);

        Log.d("TaskDelete", "doWork: " + ID);

        FirebaseDatabase.getInstance().getReference().child("check").child(UID).child(ID).removeValue();


        return Result.success();
    }
}
