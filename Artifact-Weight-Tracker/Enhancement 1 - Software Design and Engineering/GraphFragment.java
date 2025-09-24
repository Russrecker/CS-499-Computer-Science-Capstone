package com.weighttracker.app.ui.graph;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.weighttracker.app.R;
import com.weighttracker.app.data.GoalDatabase;
import com.weighttracker.app.data.WeightDatabase;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.WeightEntry;

/**
 * This class shows a line graph for the weight entries,
 * the dates, and shows the goal line.
 */
public class GraphFragment extends Fragment {

    private LineChart chart;
    private WeightDatabase weightDatabase;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy", java.util.Locale.US);
    private static java.time.LocalDate parseDate(String s) {
        try {
            return java.time.LocalDate.parse(s, FORMATTER);
        }
        catch (Exception e) {
            return java.time.LocalDate.MIN;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        chart = view.findViewById(R.id.line_graph);

        // Chart setup
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);

        weightDatabase = new WeightDatabase(requireContext());

        // setup chart view and load data
        setData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // reload data when visible
        setData();
    }

    /**
     * Gets data and builds the points and line on the graph
     */
    private void setData() {
        ArrayList<WeightEntry> list = getWeights();
        if (list.isEmpty()) {
            chart.clear();
            return;
        }

        // list of dates used by the marker bubble
        ArrayList<String> dates = new ArrayList<>(list.size());

        // Builds the points on the graph
        ArrayList<Entry> points = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            dates.add(list.get(i).getDate());

            float y = 0f;
            try {
                y = Float.parseFloat(list.get(i).getWeight().replace(" lbs","").trim());
            }
            catch (Exception ignore) {

            }
            points.add(new Entry(i, y));
        }

        // For adjusting the line and circles
        LineDataSet dataSet = new LineDataSet(points, "");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4.0f);
        dataSet.setCircleHoleRadius(3f);
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.color_fade, requireContext().getTheme()));

        // Show weight values on the graph points
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12f);

        // Color of the line and circles
        int blue = getResources().getColor(R.color.blue, null);
        dataSet.setColor(blue);
        dataSet.setCircleColor(blue);
        dataSet.setValueTextColor(blue);

        // Turns off horizontal highlight lines when selecting point
        dataSet.setDrawHorizontalHighlightIndicator(false);

        // Keep this vertical highlight line on
        dataSet.setDrawVerticalHighlightIndicator(true);
        dataSet.setHighLightColor(getResources().getColor(R.color.blue, null));
        dataSet.enableDashedHighlightLine(20f, 20f, 0f);

        // Needed to show marker
        dataSet.setHighlightEnabled(true);
        chart.setDrawMarkers(true);

        // For the y axis
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setSpaceTop(30f);
        yAxis.setDrawLabels(false);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawLimitLinesBehindData(true);

        // Clear any old goal line first
        yAxis.removeAllLimitLines();

        // Adds a goal line to y axis and get the value
        float goal = goalLine(yAxis);

        chart.setData(new LineData(dataSet));

        // Gives more room for the date bubble at the top
        chart.setExtraTopOffset(30f);

        // Keep the goal line always in view
        if (goal > 0f) {
            float dataMin = dataSet.getYMin();
            float dataMax = dataSet.getYMax();

            float low  = Math.min(goal, dataMin);
            float high = Math.max(goal, dataMax);

            float pad = Math.max(2f, (high - low) * 0.10f);

            yAxis.setAxisMinimum(Math.max(0f, low - pad));
            yAxis.setAxisMaximum(high + pad);
            yAxis.setSpaceTop(0f);
        }

        else {
            // No goal, remove the line and let the chart auto scale
            yAxis.removeAllLimitLines();
            yAxis.resetAxisMinimum();
            yAxis.resetAxisMaximum();
            yAxis.setSpaceTop(30f);
        }

        // Set bubble that shows date on tap
        DateBubble bubble = new DateBubble(requireContext(), R.layout.date_bubble, dates);
        chart.setMarker(bubble);

        // Animates chart drawing
        chart.animateX(800);

        // Show limited number of entries at once
        chart.setVisibleXRangeMaximum(6);

        // For the x axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(-0.9f);
        xAxis.setAxisMaximum(points.size() - 1f + 0.9f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.enableGridDashedLine(20f, 20f, 0f);
        int gray = getResources().getColor(R.color.gray, null);
        xAxis.setGridColor(gray);

        // Hide labels on the bottom
        xAxis.setDrawLabels(false);
    }

    /**
     * Reads the weights of the user
     *
     * @return list of WeightEntry objects, newest first. empty if none
     */
    private ArrayList<WeightEntry> getWeights() {
        ArrayList<WeightEntry> out = new ArrayList<>();

        SharedPreferences prefs = requireActivity().getSharedPreferences("myprefs", Context.MODE_PRIVATE);
        String user = prefs.getString("logged_in_username", "");

        try (Cursor cursor = weightDatabase.getUserWeights(user)) {
            int colId = cursor.getColumnIndexOrThrow("_id");
            int colDate = cursor.getColumnIndexOrThrow("date");
            int colWeight = cursor.getColumnIndexOrThrow("weight");
            while (cursor.moveToNext()) {
                out.add(new WeightEntry(
                        cursor.getInt(colId),
                        cursor.getString(colDate),
                        cursor.getString(colWeight)
                ));
            }
        }

        // Sets the graph from oldest to newest
        out.sort(Comparator.comparing(a -> parseDate(a.getDate())));
        return out;
    }

    /**
     * Adds a goal line to the graph if the user set it
     *
     * @param yAxis left axis to draw the line on
     * @return goal weight in lbs, or -1f if not set
     */
    private float goalLine(YAxis yAxis) {
        float goal = -1f;

        SharedPreferences prefs = requireActivity().getSharedPreferences("myprefs", Context.MODE_PRIVATE);
        String user = prefs.getString("logged_in_username", "");

        try (GoalDatabase goalDatabase = new GoalDatabase(requireContext())) {
            goal = goalDatabase.getGoalWeight(user);
        }

        catch (Exception ignore) {

        }

        yAxis.removeAllLimitLines();
        if (goal > 0f) {
            // Sets the color and style of the goal line
            LimitLine line = new LimitLine(goal, "\uD83C\uDFC6");  // Trophy unicode
            line.setLineColor(ResourcesCompat.getColor(getResources(), R.color.gray, null));
            line.setLineWidth(1.5f);
            line.enableDashedLine(12f, 12f, 0f);
            line.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
            line.setTextSize(18f);
            yAxis.addLimitLine(line);
        }

        return goal;
    }

    /**
     * Bubble view that shows the date when tapping a point
     */
    private static class DateBubble extends MarkerView {
        private final TextView dateText;
        private final List<String> dateList;
        private MPPointF offset;

        // Sets up the bubble view and date list
        DateBubble(Context context, int layoutRes, List<String> dates) {
            super(context, layoutRes);
            this.dateList = dates;
            dateText = findViewById(R.id.bubble);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            int i = Math.round(e.getX());
            if (i >= 0 && i < dateList.size()) {
                // Set the date text for this point
                dateText.setText(dateList.get(i));
            }
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            if (offset == null) {
                // Used to offset the bubble marker so the arrow lines up centered
                offset = new MPPointF(-(getWidth() / 12f), -getHeight() - 50f);
            }
            return offset;
        }
    }
}