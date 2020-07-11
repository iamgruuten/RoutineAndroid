package com.mad.p03.np2020.routine.Card;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mad.p03.np2020.routine.Card.Fragments.CheckListFragment;
import com.mad.p03.np2020.routine.Card.Fragments.NotesFragment;
import com.mad.p03.np2020.routine.DAL.TaskDBHelper;
import com.mad.p03.np2020.routine.R;
import com.mad.p03.np2020.routine.models.Task;

import org.w3c.dom.Text;

/**
*
* CardActivity class used to manage card activities
*
* @author Jeyavishnu & Pritheev
 *@since 10-07-2020
*
 */

public class CardActivity extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private final String TAG = "CardActivity";

    //Member Variable
    Task mTask;
    EditText mEdTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_layout);

        //Get task object from extra
        mTask = (Task) getIntent().getSerializableExtra("task");

        //IDs
        mEdTitle = findViewById(R.id.title);
        LinearLayout check = findViewById(R.id.ll_check);
        LinearLayout focus = findViewById(R.id.ll_focus);
        LinearLayout schedule = findViewById(R.id.ll_schedule);
        LinearLayout notes = findViewById(R.id.ll_notes);
        ImageView imageView = findViewById(R.id.imgEdit);

        //Set onclick listeners
        check.setOnClickListener(this);
        focus.setOnClickListener(this);
        schedule.setOnClickListener(this);
        notes.setOnClickListener(this);
        imageView.setOnClickListener(this);

        //Set editor listener
        mEdTitle.setOnEditorActionListener(this);

        //Set the title text the task name
        mEdTitle.setText(mTask.getName());

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {

        if(view.getId() != R.id.imgEdit) {
            //Doesn't have to be visible if a tool has been selected
            findViewById(R.id.toolMessage).setVisibility(View.GONE);

            //Since there is tool selected the fragment can be visible
            findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
        }
        switch (view.getId()){
            case R.id.ll_check:
                Log.i(TAG, "onClick: Check List is clicked");
                checkList();
                break;            
            case R.id.ll_focus:
                Log.i(TAG, "onClick: Focus is clicked");
                focus();
                break; 
            case R.id.ll_schedule:
                Log.i(TAG, "onClick: Schedule is clicked");
                schedule();
                break;
            case R.id.ll_notes:
                Log.i(TAG, "onClick: Notes is clicked");
                notes();
                break;
            case R.id.imgEdit:
                Log.i(TAG, "onClick: The edit button is clicked");
                showKeyboard(mEdTitle);
                break;
                
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_SEARCH ||
                i == EditorInfo.IME_ACTION_DONE ||
                keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

            Log.d(TAG, "onEditorAction(): Updating the task name too " + textView.getText());

            hideKeyboard(textView);

            //Set the text to the object
            mTask.setName(textView.getText().toString());

            //Update the SQL
            TaskDBHelper taskDBHelper = new TaskDBHelper(this);
            taskDBHelper.update(mTask);

            //Update firebase of the change name
            mTask.executeUpdateFirebase(this);


            return true;
        }

        return false;

    }

    private void schedule() {
    }

    private void notes() {
        //Initializing fragment manager and fragment transaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //Creating an instance of the notes fragment class
        NotesFragment noteFrag = new NotesFragment(mTask);

        //Replacing FragmentContainer in CardLayout with notes fragment view
        fragmentTransaction.replace(R.id.fragmentContainer, noteFrag);

        //Committing to enable fragment view
        fragmentTransaction.commit();
    }

    private void focus() {
    }

    private void checkList() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        CheckListFragment fragment = new CheckListFragment(mTask.getTaskID());
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }

    /**
     *
     * Show the keyboard the the focused view
     *
     * @param view The view that wants to receive the soft keyboard input
     */
    private void showKeyboard(View view){
        Log.i(TAG, "Show soft keyboard");

        view.setFocusableInTouchMode(true);
        view.setClickable(true);

        //Set the cursor to the end
        ((EditText)view).setSelection(((EditText)view).getText().length());

        view.requestFocus();

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert mgr != null;
        mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard(View view){
        //auto hide keyboard after entry
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        //Set focusable and clickable to false
        view.setFocusableInTouchMode(false);
        view.setClickable(false);
        view.setFocusable(false);

    }



}