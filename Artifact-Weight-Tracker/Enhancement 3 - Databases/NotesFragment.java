package com.weighttracker.app.ui.notes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.weighttracker.app.data.NotesDatabase;
import com.weighttracker.app.databinding.FragmentNotesBinding;

import java.util.ArrayList;
import java.util.List;

import model.NoteEntry;

/**
 * Shows the users notes and lets them add, edit, and delete.
 *
 */
public class NotesFragment extends Fragment implements AddNoteDialogFragment.AddNoteDialogListener {

    private FragmentNotesBinding binding;
    private final List<NoteEntry> noteList = new ArrayList<>();
    private NotesAdapter adapter;
    private NotesDatabase noteDb;
    private static final String PREFS = "myprefs";
    private static final String KEY_USERNAME = "logged_in_username";

    /**
     * Builds the screen and hooks up the list.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the root view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);
        noteDb = new NotesDatabase(requireContext());

        loadNotes();

        // list setup
        adapter = new NotesAdapter(noteList, new NotesAdapter.NoteItemActionListener() {
            @Override
            public void onEditRequested(NoteEntry note) {
                AddNoteDialogFragment dialog = AddNoteDialogFragment.newInstance(note);
                dialog.setListener(NotesFragment.this);
                dialog.show(getParentFragmentManager(), "EditNoteDialog");
            }
            @Override
            public void onDeleteRequested(NoteEntry note) {
                // When delete is clicked, show a confirm box
                deleteDialog(note);
            }
        });
        // Attach adapter
        binding.notesList.setAdapter(adapter);

        // Add note
        binding.fabAddNote.setOnClickListener(v -> {
            AddNoteDialogFragment dialog = new AddNoteDialogFragment();
            dialog.setListener(this);
            dialog.show(getParentFragmentManager(), "AddNoteDialog");
        });

        return binding.getRoot();
    }

    /**
     * Loads the notes for the user and refreshes the list.
     */
    private void loadNotes() {
        int oldSize = noteList.size();

        List<NoteEntry> newList = new ArrayList<>();
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "");

        try (Cursor cursor = noteDb.getUserNotes(username)) {
            int idIdx = cursor.getColumnIndexOrThrow("_id");
            int titleIdx = cursor.getColumnIndexOrThrow("title");
            int bodyIdx  = cursor.getColumnIndexOrThrow("body");
            int dateLastUpdatedIdx = cursor.getColumnIndexOrThrow("dateLastUpdated");

            while (cursor.moveToNext()) {
                newList.add(new NoteEntry(
                        cursor.getInt(idIdx),
                        cursor.getString(titleIdx),
                        cursor.getString(bodyIdx),
                        cursor.getLong(dateLastUpdatedIdx)
                ));
            }
        }

        // Replace list
        noteList.clear();
        noteList.addAll(newList);

        if (adapter != null) {
            if (oldSize > 0) adapter.notifyItemRangeRemoved(0, oldSize);
            if (!noteList.isEmpty()) adapter.notifyItemRangeInserted(0, noteList.size());
        }

        // Show or hide no notes text
        if (binding != null) {
            binding.emptyNotes.setVisibility(noteList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Shows confirm dialog before deleting.
     *
     * @param note the note to delete
     */
    private void deleteDialog(NoteEntry note) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    noteDb.deleteNote(note.getId());
                    int position = noteList.indexOf(note);
                    if (position >= 0) {
                        noteList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Called after saving a note.
     * Reloads the list.
     */
    @Override
    public void onNoteSaved() {
        loadNotes();
    }

    /**
     * Runs when screen is destroyed.
     * Clears the binding.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
