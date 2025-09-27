# Enhancement 2 – Algorithms and Data Structures

## Overview
For this enhancement, I added a **merge sort** to the weight entries.  
The merge sort lets users sort the weight list by date to either newest to oldest or oldest to newest.
This makes the app easier to use because people can view their progress in the order that works best for them.

## New Files
- `weight_log_menu.xml` – Adds the Sort button to the toolbar.
- `sort_order_popup.xml` – Popup menu with two options: Newest or Oldest.

## Modified Files (Snippets)

### WeightFragment.java
I added merge sort and a menu option to switch the sort order. These are the parts I added for sorting:

```java
// at top
private boolean showOldestFirst = false;
private static final String KEY_SORT_OLDEST_FIRST = "sort_oldest_first";
private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("M/d/yyyy", java.util.Locale.US);

// in onCreateView(), after loadEntries()
sortEntries();

// sort logic
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

// merge sort over dates
private List<WeightEntry> mergeSort(List<WeightEntry> arr) {
        if (arr == null || arr.size() <= 1) {
            return arr == null ? new ArrayList<>() : new ArrayList<>(arr);
        }
        int mid = arr.size() / 2;
        List<WeightEntry> left  = mergeSort(new ArrayList<>(arr.subList(0, mid)));
        List<WeightEntry> right = mergeSort(new ArrayList<>(arr.subList(mid, arr.size())));
        return merge(left, right);
    }

private List<WeightEntry> merge(List<WeightEntry> left, List<WeightEntry> right) {
        ArrayList<WeightEntry> result = new ArrayList<>(left.size() + right.size());
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            LocalDate dateLeft  = parseDate(left.get(i).getDate());
            LocalDate dateRight = parseDate(right.get(j).getDate());

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

private LocalDate parseDate(String monthDayYear) {
        try {
            return LocalDate.parse(monthDayYear, FORMATTER);
        } catch (Exception ignore) {
            return LocalDate.MIN;
        }
    }
```
#### For the menu:
```java
// in onCreateView(), inside addMenuProvider(), inside onMenuItemSelected()
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

// Save
requireActivity()
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_SORT_OLDEST_FIRST, showOldestFirst)
        .apply();

    sortEntries();
    return true;
});
```
### fragment_weight.xml

Added a hidden view as the anchor for the popup menu:

```java
<!-- Sort anchor -->
    <View
        android:id="@+id/sortMenuAnchor"
        android:layout_width="1dp"
        android:layout_height="1dp"
        android:importantForAccessibility="no"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_margin="8dp"/>
```
