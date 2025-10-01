# Enhancement 3 – Databases

## Overview
For this enhancement, I added a **Notes Database**. This lets users create, read, update and delete notes. 
Users can keep track of extra details along with their weight.

## New Files
- `NotesDatabase` – Sets up the notes table and runs add, edit, delete, and get queries.
- `AddNoteDialogFragament` – Popup dialog for adding or editing a note.
- `NotesAdapter` – Connects the note list to the RecyclerView and handles edit/delete..
- `NotesFragment` – Screen that shows all notes and connects the database with the UI..
- `NoteEntry` - Holds the data for a note (id, title, body, last updated)..
- `add_note_dialog.xml` - Layout for the add/edit note popup..
- `fragment_notes.xml` - Layout for the notes list screen..
- `list_of_notes.xml` - Layout for each note row with title, body, time, and delete button.

## Modified Files
- `bottom_nav_menu.xml` – Added a menu item for the Notes screen.
- `MainActivity.java` – Linked the Notes screen to the nav bar.

## Code from Database (Snippets)

### NotesDatabase.java - Create Table

```java
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
```

### NotesDatabase.java - Add Note

```java
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
```

### NotesDatabase.java - Get Notes

```java
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
```
