# Enhancement 1 – Software Design and Engineering

## Overview
For this enhancement, I added a **line graph** to the Weight Tracker app.  
The graph helps users see their weight changes over time.  
This makes the app easier to use and shows I can design features that improve the user experience.

## New Files
- `GraphFragment.java` – Fragment that handles the line chart.  
- `fragment_graph.xml` – Layout for the graph screen.  

## Modified Files
- `bottom_nav_menu.xml` - Added a menu item for the Graph screen.
- `mobile_navigation.xml` - Registered the new graph fragment
- `MainActivity.java` - Linked the Graph screen to the nav bar.

## Code from Graph (Snippets)

GraphFragment.java - Get Weights and Sort by Date

```java
out.sort(Comparator.comparing(a -> parseDate(a.getDate())));
return out;
```

GraphFragment.java - Build the Points and Line

```java
LineDataSet dataSet = new LineDataSet(points, "");
dataSet.setLineWidth(2f);
dataSet.setCircleRadius(4.0f);
dataSet.setDrawFilled(true);
chart.setData(new LineData(dataSet));
```

GraphFragment.java - Add Goal Line

```java
LimitLine line = new LimitLine(goal, "\uD83C\uDFC6");
line.setLineColor(ResourcesCompat.getColor(getResources(), R.color.gray, null));
yAxis.addLimitLine(line);
```

GraphFragment.java - Date Bubble

```java
DateBubble bubble = new DateBubble(requireContext(), R.layout.date_bubble, dates);
chart.setMarker(bubble);
```
