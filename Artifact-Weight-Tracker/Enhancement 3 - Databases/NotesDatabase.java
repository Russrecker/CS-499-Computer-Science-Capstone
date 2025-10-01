package com.weighttracker.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This sets up the database for saving notes for each user.
 */
public class NotesDatabase extends SQLiteOpenHelper {

    // Name of the notes database and version number
    private static final String DATABASE_NAME = "notes.db";
    private static final int VERSION = 3;

    /**
     * Creates or opens the notes database.
     *
     * @param context The context of the app using this database.
     */
    public NotesDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    /**
     * Holds the table and column names for notes.
     */
    private static final class noteTable {
        private static final String TABLE = "notes";
        private static final String col_id = "_id";
        private static final String col_username = "username";
        private static final String col_title = "title";
        private static final String col_body = "body";
        private static final String col_dateCreated = "dateCreated";
        private static final String col_dateLastUpdated = "dateLastUpdated";
    }

    /**
     * Runs the first time the database is created.
     * Sets up the table for saving the notes.
     *
     * @param db The database where the table will be created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + noteTable.TABLE + " (" +
                noteTable.col_id + " integer primary key autoincrement, " +
                noteTable.col_username + " text, " +
                noteTable.col_title + " text, " +
                noteTable.col_body + " text, " +
                noteTable.col_dateCreated + " integer, " +
                noteTable.col_dateLastUpdated + " integer)");
    }

    /**
     * Runs when the database version changes.
     * Deletes the old table and creates a new one.
     *
     * @param db The database.
     * @param oldVersion The previous version number.
     * @param newVersion The new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + noteTable.TABLE);
        onCreate(db);
    }

    /**
     * Adds a new note for the user.
     *
     * @param username The user who the note belongs to.
     * @param title The title of the note.
     * @param body The body of the note.
     * @return The row ID of the inserted entry, or -1 if it failed.
     */
    public long addNote(String username, String title, String body) {
        long timestamp = System.currentTimeMillis();
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(noteTable.col_username, username);
        values.put(noteTable.col_title, title);
        values.put(noteTable.col_body, body);
        values.put(noteTable.col_dateCreated, timestamp);
        values.put(noteTable.col_dateLastUpdated, timestamp);

        return db.insert(noteTable.TABLE, null, values);
    }

    /**
     * Gets all notes for the user, ordered by newest first.
     *
     * @param username The username to look up.
     * @return A Cursor pointing to the users notes record.
     */
    public Cursor getUserNotes(String username) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + noteTable.TABLE +
                " WHERE " + noteTable.col_username + " = ?" +
                " ORDER BY " + noteTable.col_dateLastUpdated + " DESC";

        return db.rawQuery(sql, new String[]{username});
    }

    /**
     * Deletes a note by ID.
     *
     * @param id The ID of the entry to delete.
     */
    public void deleteNote(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(noteTable.TABLE, noteTable.col_id + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * Updates a note with new info.
     *
     * @param id The ID of the note to update.
     * @param title The title of the note to update
     * @param body The body of the note to update
     * @return true if the update worked, false if it didn't.
     */
    public boolean updateNote(int id, String title, String body) {
        long timestamp = System.currentTimeMillis();
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(noteTable.col_title, title);
        values.put(noteTable.col_body, body);
        values.put(noteTable.col_dateLastUpdated, timestamp);

        int rows = db.update(noteTable.TABLE, values, noteTable.col_id + " = ?", new String[]{String.valueOf(id)});
        db.close();

        return rows > 0;
    }


}
