package com.hvl.dragonteam.Interface;

import android.view.DragEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.hvl.dragonteam.Adapter.LineupAdapter;
import com.hvl.dragonteam.Adapter.LineupTeamAdapter;
import com.hvl.dragonteam.Model.LineupItem;
import com.hvl.dragonteam.Model.PersonTrainingAttendance;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.App;

import java.util.ArrayList;
import java.util.List;

public class DragListener implements View.OnDragListener {

    private boolean isDropped = false;
    private OnLineupChangeListener listener;

    public DragListener(OnLineupChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {

        switch (event.getAction()) {
            case DragEvent.ACTION_DROP:
                int positionTarget = -1;

                View viewSource = (View) event.getLocalState();
                int viewId = v.getId();

                final int listViewLineup = R.id.listView_lineup;
                final int layoutLineup = R.id.layout_lineup;
                final int listViewPerson = R.id.listView_team;

                switch (viewId) {
                    case listViewPerson:
                    case listViewLineup:
                    case layoutLineup:

                        RecyclerView recyclerViewTarget;
                        switch (viewId) {
                            case listViewLineup:
                                recyclerViewTarget = (RecyclerView) v.getRootView().findViewById(listViewLineup);
                                break;
                            case listViewPerson:
                                recyclerViewTarget = (RecyclerView) v.getRootView().findViewById(listViewPerson);
                                break;
                            default:
                                recyclerViewTarget = (RecyclerView) v.getParent();
                                positionTarget = (int) v.getTag();
                        }

                        if (viewSource != null) {

                            RecyclerView recyclerViewSource = (RecyclerView) viewSource.getParent();

                            if (recyclerViewSource.getAdapter() instanceof LineupAdapter && recyclerViewTarget.getAdapter() instanceof LineupAdapter) {

                                if (positionTarget >= 0) {

                                    LineupAdapter adapterSource = (LineupAdapter) recyclerViewSource.getAdapter();
                                    int positionSource = (int) viewSource.getTag();
                                    List<LineupItem> listSource = adapterSource.getListLineup();
                                    LineupItem listItemSource = listSource.get(positionSource);

                                    LineupAdapter adapterTarget = (LineupAdapter) recyclerViewTarget.getAdapter();
                                    List<LineupItem> listTarget = adapterTarget.getListLineup();
                                    LineupItem listItemTarget = listSource.get(positionTarget);

                                    LineupItem newTargetItem = new LineupItem(listItemTarget.getId(), listItemSource.getPersonTrainingAttendance());
                                    listTarget.set(positionTarget, newTargetItem);

                                    adapterTarget.updateListLineup((ArrayList<LineupItem>) listTarget);
                                    adapterTarget.notifyDataSetChanged();

                                    LineupItem newSourceItem = new LineupItem(listItemSource.getId(), listItemTarget.getPersonTrainingAttendance());
                                    listSource.set(positionSource, newSourceItem);
                                    adapterSource.updateListLineup((ArrayList<LineupItem>) listSource);
                                    adapterSource.notifyDataSetChanged();

                                    isDropped = true;
                                }
                            } else if (recyclerViewSource.getAdapter() instanceof LineupTeamAdapter && recyclerViewTarget.getAdapter() instanceof LineupAdapter) {

                                if (positionTarget >= 0) {

                                    LineupTeamAdapter adapterSource = (LineupTeamAdapter) recyclerViewSource.getAdapter();
                                    int positionSource = (int) viewSource.getTag();
                                    List<PersonTrainingAttendance> listSource = adapterSource.getListLineupPerson();
                                    PersonTrainingAttendance listItemSource = listSource.get(positionSource);

                                    LineupAdapter adapterTarget = (LineupAdapter) recyclerViewTarget.getAdapter();
                                    List<LineupItem> listTarget = adapterTarget.getListLineup();
                                    LineupItem listItemTarget = listTarget.get(positionTarget);

                                    LineupItem newTargetItem = new LineupItem(positionTarget, listItemSource);
                                    listTarget.set(positionTarget, newTargetItem);

                                    adapterTarget.updateListLineup((ArrayList<LineupItem>) listTarget);
                                    adapterTarget.notifyDataSetChanged();

                                    if (listItemTarget.getPersonTrainingAttendance().getPersonId() != App.getContext().getString(R.string.empty)) {
                                        listSource.set(positionSource, listItemTarget.getPersonTrainingAttendance());
                                    } else {
                                        listSource.remove(positionSource);
                                    }
                                    adapterSource.updateListLineupPerson((ArrayList<PersonTrainingAttendance>) listSource);
                                    adapterSource.notifyDataSetChanged();
                                    isDropped = true;
                                }
                            } else if (recyclerViewSource.getAdapter() instanceof LineupAdapter && recyclerViewTarget.getAdapter() instanceof LineupTeamAdapter) {

                                LineupAdapter adapterSource = (LineupAdapter) recyclerViewSource.getAdapter();
                                int positionSource = (int) viewSource.getTag();
                                List<LineupItem> listSource = adapterSource.getListLineup();
                                LineupItem listItemSource = listSource.get(positionSource);

                                if (!listItemSource.getPersonTrainingAttendance().getPersonId().equals(App.getContext().getString(R.string.empty))) {

                                    LineupTeamAdapter adapterTarget = (LineupTeamAdapter) recyclerViewTarget.getAdapter();
                                    List<PersonTrainingAttendance> listTarget = adapterTarget.getListLineupPerson();
                                    PersonTrainingAttendance listItemTarget;
                                    PersonTrainingAttendance newPersonTrainingAttendance = listItemSource.getPersonTrainingAttendance();

                                    if (positionTarget >= 0) {
                                        listItemTarget = listTarget.get(positionTarget);
                                        listTarget.set(positionTarget, newPersonTrainingAttendance);
                                    } else {
                                        listItemTarget = new PersonTrainingAttendance();
                                        listItemTarget.setName(App.getContext().getString(R.string.empty));
                                        listItemTarget.setPersonId(App.getContext().getString(R.string.empty));
                                        listTarget.add(newPersonTrainingAttendance);
                                    }

                                    adapterTarget.updateListLineupPerson((ArrayList<PersonTrainingAttendance>) listTarget);
                                    adapterTarget.notifyDataSetChanged();

                                    LineupItem newTargetItem = new LineupItem(positionSource, listItemTarget);
                                    listSource.set(positionSource, newTargetItem);
                                    adapterSource.updateListLineup((ArrayList<LineupItem>) listSource);
                                    adapterSource.notifyDataSetChanged();

                                    isDropped = true;
                                }
                            }
                        }
                        break;
                }
                break;
        }

        if (!isDropped && event.getLocalState() != null) {
            ((View) event.getLocalState()).setVisibility(View.VISIBLE);
        }

        if(isDropped){
            listener.lineupChange();
        }
        return true;

    }
}