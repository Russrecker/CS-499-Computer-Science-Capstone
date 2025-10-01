package com.weighttracker.app.ui.notes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.weighttracker.app.R;
import com.weighttracker.app.data.NotesDatabase;

import model.NoteEntry;

/**
 * This is the screen where the user adds or edits a note.
 */
public class AddNoteDialogFragment extends DialogFragment {

    private AddNoteDialogListener listener;
    private NotesDatabase noteDb;
    private NoteEntry existingNote;

    /**
     * Lets the screen get the new note after its saved.
     */
    public interface AddNoteDialogListener {
        void onNoteSaved();
    }

    /**
     * This sets who gets the new note info after its added.
     */
    public void setListener(AddNoteDialogListener listener) {
        this.listener = listener;
    }

    /**
     * Creates the add and edit note dialog.
     * If editing, it fills in the notes title and body.
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return the dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        noteDb = new NotesDatabase(requireContext());

        // Check if editing a note
        if (getArguments() != null) {
            existingNote = new NoteEntry(
                    getArguments().getInt("id"),
                    getArguments().getString("title", ""),
                    getArguments().getString("body", ""),
                    getArguments().getLong("dateLastUpdated", 0L)
            );
        }

        // Load the add note screen layout
        View view = getLayoutInflater().inflate(R.layout.add_note_dialog, null);
        EditText titleEdit = view.findViewById(R.id.title);
        EditText bodyEdit = view.findViewById(R.id.body);

        // Fill fields if we are editing an existing note
        if (existingNote != null) {
            titleEdit.setText(existingNote.getTitle());
            bodyEdit.setText(existingNote.getBody());
        }

        // Create the dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(existingNote == null ? R.string.add_note_dialog : R.string.edit_note_dialog)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();

        // Save button click
        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = titleEdit.getText().toString().trim();
            String body  = bodyEdit.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean saved;
            if (existingNote == null) {
                // create new note for the user
                String username = requireActivity()
                        .getSharedPreferences("myprefs", Context.MODE_PRIVATE)
                        .getString("logged_in_username", "");
                saved = noteDb.addNote(username, title, body) != -1;
            } else {
                // update note by id
                saved = noteDb.updateNote(existingNote.getId(), title, body);
            }

            if (saved) {
                if (listener != null) listener.onNoteSaved();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Could not save note", Toast.LENGTH_SHORT).show();
            }
        }));

        return dialog;
    }

    /**
     * Creates a new dialog instance when editing a note.
     *
     * @param note The entry to pre fill.
     * @return A filled out dialog ready to display.
     */
    public static AddNoteDialogFragment newInstance(NoteEntry note) {
        AddNoteDialogFragment fragment = new AddNoteDialogFragment();
        Bundle args = new Bundle();
        args.putInt("id", note.getId());
        args.putString("title", note.getTitle());
        args.putString("body", note.getBody());
        args.putLong("dateLastUpdated", note.getDateLastUpdated());
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Runs when the dialog connects to the screen.
     * Makes sure the screen can listen for saved notes.
     *
     * @param context The screen this dialog is attached to.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Only try to set the listener if it hasn't been set manually
        if (listener == null) {
            Fragment parent = getParentFragment();
            if (parent instanceof AddNoteDialogListener) {
                listener = (AddNoteDialogListener) parent;
            }
        }
    }
}
