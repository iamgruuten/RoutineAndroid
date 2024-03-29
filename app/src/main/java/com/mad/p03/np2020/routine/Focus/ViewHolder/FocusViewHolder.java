package com.mad.p03.np2020.routine.Focus.ViewHolder;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.p03.np2020.routine.Focus.Adapter.FocusAdapter;
import com.mad.p03.np2020.routine.Focus.Model.Focus;
import com.mad.p03.np2020.routine.R;

import java.text.ParseException;

/**
 *
 * ViewHolder used to manage the recyclerViewAdapter
 *
 * @author Lee Quan Sheng
 * @since 02-06-2020
 */
public class FocusViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ImageView iconComplete;
    public TextView Task, date, duration;
    public FocusAdapter adapter;
    public ViewGroup parent;
    public ConstraintLayout constraintLayout;
    AlertDialog.Builder builder;
    AlertDialog dialog;

    /**
     *
     * FocusViewHolder for custom RecyclerView
     * an item view and metadata about its place within the RecyclerView
     *
     * @param itemView set the positon to this section
     * @param adapter set the adapter to this section
     * @param parent set the parent to this section
     *
     * */
    public FocusViewHolder(@NonNull View itemView, FocusAdapter adapter, ViewGroup parent) {
        super(itemView);
        iconComplete = itemView.findViewById(R.id.icon);
        Task = itemView.findViewById(R.id.taskView);
        date = itemView.findViewById(R.id.dateView);
        duration = itemView.findViewById(R.id.duration);
        constraintLayout = itemView.findViewById(R.id.relativeLayout);

        this.adapter = adapter;
        this.parent = parent;

        itemView.setOnClickListener(this);
    }

    /**
     *
     * Onclick function if the viewHolder is clicked
     *
     * @param v Set View to this context
     *
     * */
    @Override
    public void onClick(View v) { //If item on click
        if(dialog == null || !
                dialog.isShowing()) {
            showAlertDialogButtonClicked(this.getLayoutPosition(), adapter.getItems(getLayoutPosition()));
            Log.v("item", "Item on click");
        }
    }

    /**
     *
     * Show custom delete item AlertDialog
     *
     * @param position set the positon to this section
     * @param focus set the focus to this section
     *
     * */

    public void showAlertDialogButtonClicked(final int position, final Focus focus) {
        final String task = Task.getText().toString();


        builder = new AlertDialog.Builder(itemView.getContext(), R.style.MyDialogTheme);

        builder.setTitle("Delete");

        LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
        View customLayout = inflater.inflate(R.layout.custom_delete_layout, null);

        //Used for alertDialog subText
        TextView staskName = customLayout.findViewById(R.id.taskName);
        staskName.setText(task);

        builder.setView(customLayout);

        //revert changes
        builder.setNegativeButton("No", null);

        //Deletion to the view
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Log.v("AlertDialog", "Delete Item " + task);
            try {
                adapter.remove(position, focus);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        // create and show the alert dialog
        dialog = builder.create();

        dialog.show();
    }
}