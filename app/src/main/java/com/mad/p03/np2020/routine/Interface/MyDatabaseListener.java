package com.mad.p03.np2020.routine.Interface;

import com.mad.p03.np2020.routine.Class.Section;

/**
 * 
 * This interface defines the onDataAdd and OnDataDelete which i will like to
 * communicate with my owner
 */
public interface MyDatabaseListener {

    public void onDataAdd(Object object);
    public void onDataDelete(String ID);
    public void onDataUpdate(Object object);
}
