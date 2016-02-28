package com.vomtom.refugeebuddy.listeners;

import android.database.sqlite.SQLiteDatabase;

public interface OnDatabaseOpenedListener {
    void onOpened(SQLiteDatabase db);
    void onError(Exception exception);
}