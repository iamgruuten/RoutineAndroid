package com.mad.p03.np2020.routine.Card.ViewHolder;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mad.p03.np2020.routine.Card.models.CardTouchHelperAdapter;
import com.mad.p03.np2020.routine.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 *
 * This is part of the custom calender to show all the dates
 *
 * @author Jeyavishnu
 * @since 02-08-2020
 *
 */
public class MyCheckViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, GestureDetector.OnGestureListener {


    public TextView mListName;
    public CheckBox mCheckBox;

    private static  String TAG = "CardAdapter";

    private ItemTouchHelper mItemTouchHelper;
    private GestureDetector mGestureDetector;

    private final CardTouchHelperAdapter mCardTouchHelperAdapter;

    public MyCheckViewHolder(@NonNull View itemView, ItemTouchHelper itemTouchHelper , CardTouchHelperAdapter cardTouchHelperAdapter) {
        super(itemView);

        //Find the id
        mListName = itemView.findViewById(R.id.txtListName);
        mCheckBox = itemView.findViewById(R.id.checkbox);

        mCardTouchHelperAdapter = cardTouchHelperAdapter;


        this.mItemTouchHelper = itemTouchHelper;

        mGestureDetector = new GestureDetector(itemView.getContext(), this);
        itemView.setOnTouchListener(this);

    }

    /**
     *
     * Not implemented
     *
     * Notify when the a tap occurs on the view. This will
     * trigger immediately for every down event
     *
     * @param motionEvent The down motion event.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        Log.d(TAG, "onDown: Clicked Down");
        return false;
    }

    /**
     * Not implemented
     *
     * The user has performed a down MotionEvent and not performed a move or up yet.
     * This event is commonly used to provide visual feedback to the user to let them
     * know that their action has been recognized i.e. highlight an element.
     *
     * @param motionEvent The down motion event
     */
    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    /**
     *
     * This to notify when the tap occurs with the up motion triggered.
     * This is where we trigger {@code onItemClicked(int position) } and pass the position
     *
     * @param motionEvent The up motion event that completed the first tap
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        Log.d(TAG, "onSingleTapUp: Clicked and the motion is up");
        mCardTouchHelperAdapter.onItemClicked(getAdapterPosition());
        return false;
    }

    /**
     *
     * Not Implemented
     *
     * Notified when a scroll occurs with the initial on down MotionEvent and the current move MotionEvent.
     * The distance in x and y is also supplied for convenience.
     *
     * @param motionEvent The first down motion event that started the scrolling.
     * @param motionEvent1 The move motion event that triggered the current onScroll.
     * @param v The distance along the X axis that has been scrolled since the last call
     *          to onScroll. This is NOT the distance between e1 and e2.
     * @param v1  The distance along the Y axis that has been scrolled since the last call to onScroll.
     *            This is NOT the distance between e1 and e2.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        Log.d(TAG, "onScroll(): Scrolling ");
        return false;
    }

    /**
     *
     * Notified when a long press occurs with the initial on down MotionEvent that trigged it.
     * Use this enable dragging on custom view holder (this)
     *
     * @param motionEvent The initial on down motion event that started the long press.
     */
    @Override
    public void onLongPress(MotionEvent motionEvent) {
        Log.d(TAG, "onLongPress(): Drag enabled for this custom view holder ");
        mItemTouchHelper.startDrag(this);
    }

    /**
     *
     * Not implemented
     *
     * Notified of a fling event when it occurs with the initial on down MotionEvent and the matching up MotionEvent.
     * The calculated velocity is supplied along the x and y axis in pixels per second.
     *
     * @param motionEvent  The first down motion event that started the fling.
     * @param motionEvent1 The move motion event that triggered the current onFling.
     * @param v The velocity of this fling measured in pixels per second along the x axis.
     * @param v1 The velocity of this fling measured in pixels per second along the y axis.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    /**
     *
     * Called when a touch event is dispatched to a view. This allows listeners to get a chance to respond
     * before the target view. This is used to handle touch screen motion events {@code onTouchEvent(motionEvent)}
     *
     * @param view The view the touch event has been dispatched to.
     * @param motionEvent The MotionEvent object containing full information about the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mGestureDetector.onTouchEvent(motionEvent);
        return true;
    }
}