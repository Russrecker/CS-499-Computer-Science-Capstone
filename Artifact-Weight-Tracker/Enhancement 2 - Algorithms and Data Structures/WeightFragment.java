package com.weighttracker.app.ui.weight;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.DividerItemDecoration;
import com.weighttracker.app.R;
import com.weighttracker.app.data.WeightDatabase;
import com.weighttracker.app.databinding.FragmentWeightBinding;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.WeightEntry;

/**
 * Shows all weight entries. You can add, edit, delete, and sort them by date..
 *
 */
public class WeightFragment extends Fragment implements AddWeightDialogFragment.AddWeightDialogListener {

    private FragmentWeightBinding binding;
    private final List<WeightEntry> entryList = new ArrayList<>();
    private WeightAdapter adapter;
    private WeightDatabase weightDatabase;
    private boolean showOldestFirst = false;
    private static final String KEY_SORT_OLDEST_FIRST = "sort_oldest_first";
    private static final String KEY_USERNAME = "logged_in_username";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy", java.util.Locale.US);
    private static final String PREFS = "myprefs";


    /**
     * Called when this screen is first loaded.
     * Sets up everything including the list and database.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentWeightBinding.inflate(inflater, container, false);
        weightDatabase = new WeightDatabase(requireContext());

        // Restore sort
        showOldestFirst = requireActivity()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_SORT_OLDEST_FIRST, false);

        // Load saved weight entries
        loadEntries();
        sortEntries();

        // Adapter and actions
        adapter = new WeightAdapter(entryList, new WeightAdapter.WeightItemActionListener() {
            @Override
            public void onEditRequested(WeightEntry entry) {
                AddWeightDialogFragment dialog = AddWeightDialogFragment.newInstance(entry);
                dialog.setListener(WeightFragment.this);
                dialog.show(getParentFragmentManager(), "EditWeightDialog");
            }
            @Override
            public void onDeleteRequested(WeightEntry entry) {
                // When delete is clicked, show a confirm box
                deleteDialog(entry);
            }
        });
        // Attach adapter
        binding.weightList.setAdapter(adapter);

        // Add a line between each row in the list
        DividerItemDecoration divider = new DividerItemDecoration(
                binding.weightList.getContext(),
                DividerItemDecoration.VERTICAL
        );
        binding.weightList.addItemDecoration(divider);

        // Popup menu for sorting
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                // Load the menu
                inflater.inflate(R.menu.weight_log_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_sort_order) {
                    // Open a small popup near the icon
                    View anchor = requireView().findViewById(R.id.sortMenuAnchor);
                    if (anchor == null) anchor = binding.getRoot();

                    PopupMenu popup = new PopupMenu(requireContext(), anchor);
                    popup.getMenuInflater().inflate(R.menu.sort_order_popup, popup.getMenu());

                    // Set current choice
                    popup.getMenu().findItem(R.id.menu_sort_oldest).setChecked(showOldestFirst);
                    popup.getMenu().findItem(R.id.menu_sort_newest).setChecked(!showOldestFirst);

                    popup.setOnMenuItemClickListener(menuItem -> {
                        if (menuItem.getItemId() == R.id.menu_sort_oldest) {
                            showOldestFirst = true;
                        }
                        else if (menuItem.getItemId() == R.id.menu_sort_newest) {
                            showOldestFirst = false;
                        }
                        else {
                            return false;
                        }

                        // Update checkmark
                        popup.getMenu().findItem(R.id.menu_sort_oldest).setChecked(showOldestFirst);
                        popup.getMenu().findItem(R.id.menu_sort_newest).setChecked(!showOldestFirst);

                        // Save
                        requireActivity()
                                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(KEY_SORT_OLDEST_FIRST, showOldestFirst)
                                .apply();

                        sortEntries();
                        return true;
                    });

                    popup.show();
                    return true;
                }

                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return binding.getRoot();
    }

    /**
     * Load all weight entries for the user
     */
    private void loadEntries() {
        entryList.clear();

        // Get the username of the logged-in user
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "");

        // Grab the weights from the database for this user
        try (Cursor cursor = weightDatabase.getUserWeights(username)) {
            int idIdx = cursor.getColumnIndexOrThrow("_id");
            int dateIdx = cursor.getColumnIndexOrThrow("date");
            int wtIdx  = cursor.getColumnIndexOrThrow("weight");
            while (cursor.moveToNext()) {
                entryList.add(new WeightEntry(
                        cursor.getInt(idIdx),
                        cursor.getString(dateIdx),
                        cursor.getString(wtIdx)
                ));
            }
        }
    }

    /**
     * Shows a confirm box before deleting an entry
     *
     * @param entry the weight entry to delete
     */
    private void deleteDialog(WeightEntry entry) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this weight entry?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Find by id
                    int position = -1;
                    for (int k = 0; k < entryList.size(); k++) {
                        if (entryList.get(k).getId() == entry.getId()) {
                            position = k;
                            break;
                        }
                    }
                    if (position >= 0) {
                        weightDatabase.deleteWeight(entry.getId());
                        entryList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * This sorts the weight entries by date
     * and refreshes the list on the screen.
     */
    private void sortEntries() {
        List<WeightEntry> sortedList = mergeSort(entryList); // oldest to newest
        if (!showOldestFirst) {
            java.util.Collections.reverse(sortedList);       // newest to oldest
        }
        entryList.clear();
        entryList.addAll(sortedList);

        if (adapter != null) {
            adapter.notifyItemRangeChanged(0, entryList.size());
        }
    }

    /**
     * Merge sort order over dates.
     *
     * @param arr the list of weight entries to sort
     * @return a new list sorted by date
     */
    private List<WeightEntry> mergeSort(List<WeightEntry> arr) {
        if (arr == null || arr.size() <= 1) {
            return arr == null ? new ArrayList<>() : new ArrayList<>(arr);
        }
        int mid = arr.size() / 2;
        List<WeightEntry> left  = mergeSort(new ArrayList<>(arr.subList(0, mid)));
        List<WeightEntry> right = mergeSort(new ArrayList<>(arr.subList(mid, arr.size())));
        return merge(left, right);
    }

    /**
     * Merges two sorted lists into one.
     *
     * @param left the left side list
     * @param right the right side list
     * @return a list in order
     */
    private List<WeightEntry> merge(List<WeightEntry> left, List<WeightEntry> right) {
        ArrayList<WeightEntry> result = new ArrayList<>(left.size() + right.size());
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            LocalDate dateLeft  = convertDate(left.get(i).getDate());
            LocalDate dateRight = convertDate(right.get(j).getDate());

            // Keep order when dates match
            if (!dateLeft.isAfter(dateRight)) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }
        // Add leftovers
        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }

    /**
     * Turns "M/d/yyyy" into a LocalDate.
     *
     * @param monthDayYear the date string from the entry
     * @return a LocalDate object
     */
    private LocalDate convertDate(String monthDayYear) {
        try {
            return LocalDate.parse(monthDayYear, FORMATTER);
        } catch (Exception ignore) {
            return LocalDate.MIN;
        }
    }

    /**
     * Called when the screen is being destroyed.
     * This clears the binding to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Called when the user taps the add button.
     * Opens the add weight screen.
     */
    public void addDialog() {
        AddWeightDialogFragment dialog = new AddWeightDialogFragment();
        dialog.setListener(this);
        dialog.show(getParentFragmentManager(), "AddWeightDialog");
    }

    /**
     * This is called after a new weight entry is added.
     * It reloads the list to show the new data.
     *
     * @param date the date of the new entry
     * @param weight the weight value
     */
    @Override
    public void onWeightEntry(String date, String weight) {
        loadEntries();
        sortEntries();
    }
}