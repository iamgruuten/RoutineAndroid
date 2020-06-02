package com.mad.p03.np2020.routine.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.p03.np2020.routine.Adapter.OnItemClickListener;
import com.mad.p03.np2020.routine.R;

/**
 *
 * Model used to manage the section
 *
 * @author Hou Man
 * @since 02-06-2020
 */

public class HabitGroupHolder extends RecyclerView.ViewHolder {

    /** Shown as group name on the habitGroup holder*/
    public TextView grp_name;

    /**
     *
     * This method is used to
     *  bind the widgets to the holder and set the onClickListener interface to each holder by parsing its holder position.
     *
     * @param itemView This parameter is used to get the view of the holder.
     *
     * @param listener This parameter is used to get the listener interface.
     *
     * */
    public HabitGroupHolder(@NonNull View itemView, final OnItemClickListener listener) {
        super(itemView);

        this.grp_name = itemView.findViewById(R.id.habit_group_name);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener!=null){
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        listener.onItemClick(position);
                    }
                }
            }
        });
    }
}
