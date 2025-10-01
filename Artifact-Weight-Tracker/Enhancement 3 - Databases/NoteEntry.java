package model;

/**
 * One note entry.
 */
public class NoteEntry {
    private final int id;
    private final String title;
    private final String body;
    private final long dateLastUpdated;

    /**
     * Makes a note with id, title, body, and last updated time.
     *
     * @param id unique ID of the note in the database
     * @param title the title text of the note
     * @param body the body text of the note
     * @param dateLastUpdated the last updated time in milliseconds
     */
    public NoteEntry(int id, String title, String body, long dateLastUpdated) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.dateLastUpdated = dateLastUpdated;
    }

    /**
     * Gets the ID of this note entry.
     *
     * @return the ID as an int
     */
    public int getId() { return id; }

    /**
     * Gets the title text of this note.
     *
     * @return the title as a String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the body text of this note.
     *
     * @return the body as a String
     */
    public String getBody() {
        return body;
    }

    /**
     * Gets the time this note was last updated.
     *
     * @return the time in milliseconds
     */
    public long getDateLastUpdated() {
        return dateLastUpdated;
    }
}
