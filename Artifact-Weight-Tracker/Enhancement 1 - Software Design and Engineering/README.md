# Enhancement 1 – Software Design and Engineering

## Overview
For this enhancement, I added a **line graph** to the Weight Tracker app.  
The graph helps users see their weight changes over time.  
This makes the app easier to use and shows I can design features that improve the user experience.

## New Files
- `GraphFragment.java` – Fragment that handles the line chart.  
- `fragment_graph.xml` – Layout for the graph screen.  

## Modified Files (Snippets)

### MainActivity.java
Added the graph screen to the top-level navigation:

```java
// These are the main screens (weight, settings, graph and notes)
AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
        R.id.navigation_weight, R.id.navigation_settings, R.id.navigation_graph).build();
```

### mobile_navigation.xml

Registered the new graph fragment:

```java
<fragment
    android:id="@+id/navigation_graph"
    android:name="com.weighttracker.app.ui.graph.GraphFragment"
    android:label="@string/title_graph"
    tools:layout="@layout/fragment_graph" />
```
### bottom_nav_menu.xml

Added a menu item for the graph tab:

```java
<!-- Graph -->
<item
    android:id="@+id/navigation_graph"
    android:icon="@drawable/outline_planner_review_24"
    android:title="@string/title_graph" />
