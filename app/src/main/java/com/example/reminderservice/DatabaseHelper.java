package com.example.reminderservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.reminderservice.model.ReminderDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mitch on 2016-05-13.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "mylist_db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ReminderDB.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ReminderDB.TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String message, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ReminderDB.COLUMN_NOTE, message);
        contentValues.put(ReminderDB.COLUMN_TIMESTAMP, time);
        long id = db.insert(ReminderDB.TABLE_NAME, null, contentValues);
        db.close();

        if (id == -1) {
            return false;
        } else {
            return true;
        }

    }

    public Cursor getListContents(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + ReminderDB.TABLE_NAME + " ORDER BY " + ReminderDB.COLUMN_TIMESTAMP + " ASC", null);
        return data;
    }
    public List<ReminderDB> getAllNotes() {
        List<ReminderDB> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + ReminderDB.TABLE_NAME + " ORDER BY " +
                ReminderDB.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ReminderDB reminderNote = new ReminderDB();
                reminderNote.setId(cursor.getInt(cursor.getColumnIndex(ReminderDB.COLUMN_ID)));
                reminderNote.setReminderMsg(cursor.getString(cursor.getColumnIndex(ReminderDB.COLUMN_NOTE)));
                reminderNote.setTimestamp(cursor.getString(cursor.getColumnIndex(ReminderDB.COLUMN_TIMESTAMP)));
                notes.add(reminderNote);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return notes;
    }

    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + ReminderDB.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateNote(ReminderDB reminder) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ReminderDB.COLUMN_NOTE, reminder.getReminderMsg());
        values.put(ReminderDB.COLUMN_TIMESTAMP,reminder.getTimestamp());

        // updating row
        return db.update(ReminderDB.TABLE_NAME, values, ReminderDB.COLUMN_ID + " = ?",
                new String[]{String.valueOf(reminder.getId())});
    }

    public void deleteNote(ReminderDB reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ReminderDB.TABLE_NAME, ReminderDB.COLUMN_ID + " = ?",
                new String[]{String.valueOf(reminder.getId())});
        db.close();
    }
}