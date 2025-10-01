package com.weighttracker.app.ui.notes;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.weighttracker.app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import model.NoteEntry;

/**
 * Adapter for the notes list.
 *
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteHolder> {

    private final List<NoteEntry> noteList;
    private final NoteItemActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);

    /**
     * Constructor sets up the adapter with the note list and listener
     *
     * @param noteList list of notes to show
     * @param listener used for edit and delete actions
     */
    public NotesAdapter(List<NoteEntry> noteList, NoteItemActionListener listener) {
        this.noteList = noteList;
        this.listener = listener;
    }

    /**
     * Makes a new row for the list.
     *
     * @param parent the parent view group
     * @param viewType the type of view
     * @return a new NoteHolder
     */
    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new NoteHolder(inflater, parent);
    }

    /**
     * Puts the note data into the row.
     *
     * @param holder the row holder
     * @param position where we are in the list
     */
    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        holder.bind(noteList.get(position), listener, dateFormat);
    }

    /**
     * Tells how many items are in the list.
     *
     * @return size of the note list
     */
    @Override
    public int getItemCount() {

        return noteList.size();
    }

    /**
     * Holds each item in the list.
     */
    public static class NoteHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView body;
        private final TextView timestamp;
        private final ImageButton deleteButton;

        /**
         * Sets up the views for each row.
         *
         * @param inflater used to create the layout
         * @param parent the parent layout
         */
        public NoteHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_of_notes, parent, false));
            title = itemView.findViewById(R.id.note_title);
            body = itemView.findViewById(R.id.note_body);
            timestamp = itemView.findViewById(R.id.note_timestamp);
            deleteButton = itemView.findViewById(R.id.note_delete);
        }

        /**
         * Shows the notes title and body and last updated time.
         * Tapping anywhere edits and trash icon deletes.
         *
         * @param note the note entry to display
         * @param listener callback for edit and delete actions
         * @param sdf formatter for the timestamp
         */
        void bind(NoteEntry note, NoteItemActionListener listener, SimpleDateFormat sdf) {
            title.setText(note.getTitle());
            body.setText(note.getBody());
            timestamp.setText(sdf.format(new Date(note.getDateLastUpdated())));

            itemView.setOnClickListener(v -> listener.onEditRequested(note));
            deleteButton.setOnClickListener(v -> listener.onDeleteRequested(note));
        }
    }

    /**
     * This lets the screen know when edit or delete is tapped.
     */
    public interface NoteItemActionListener {
        void onEditRequested(NoteEntry note);
        void onDeleteRequested(NoteEntry note);
    }
}

