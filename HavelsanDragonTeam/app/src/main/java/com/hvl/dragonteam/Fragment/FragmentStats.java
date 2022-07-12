package com.hvl.dragonteam.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Bar;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.HoverMode;
import com.anychart.enums.TooltipDisplayMode;
import com.anychart.enums.TooltipPositionMode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Adapter.PersonTeamByTeamAdapter;
import com.hvl.dragonteam.DataService.StatsService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Stats;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentStats extends Fragment {

    private View view;
    private Activity activity;
    private FragmentManager fragmentManager;
    private FragmentActivity context;
    private PersonTeamByTeamAdapter personTeamAdapter;
    private ArrayList<Stats> statsList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_stats, container, false);
        activity = getActivity();
        context = (FragmentActivity) getContext();
        fragmentManager = context.getSupportFragmentManager();
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.stats));

        getStats();

        return view;
    }

    private void getStats() {
        StatsService statsService = new StatsService();
        Team team = new Team();
        team.setId(Constants.personTeamView.getTeamId());
        try {
            statsService.getStatsByPerson(context, team,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {
                            List<Stats> list = new Gson().fromJson(result.toString(), new TypeToken<List<Stats>>() {
                            }.getType());

                            statsList.clear();
                            statsList.addAll(list);
                            initChart();
                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        }

                        @Override
                        public void onSuccess(JSONObject result) {
                        }

                        @Override
                        public void onError(String result) {
                            Activity activity = getActivity();
                            if (activity != null && isAdded())
                                Util.toastError(context);
                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Activity activity = getActivity();
            if (activity != null && isAdded())
                Util.toastError(context);
            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    private void initChart(){
        AnyChartView anyChartView = view.findViewById(R.id.chart);
        //anyChartView.clear();

        Cartesian vertical = AnyChart.vertical();

        vertical.animation(true)
                .title(getString(R.string.chart_title_by_person));

        List<DataEntry> data = new ArrayList<>();

        for(Stats stats : statsList) {
            data.add(new ValueDataEntry(stats.getName(), stats.getCount()));
        }

        Set set = Set.instantiate();
        set.data(data);
        Mapping barData = set.mapAs("{ x: 'x', value: 'value' }");

        Bar bar = vertical.bar(barData);
        bar.labels().format("{%Value}");

        vertical.yScale().minimum(0d);
        vertical.labels(true);
        vertical.tooltip()
                .displayMode(TooltipDisplayMode.UNION)
                .positionMode(TooltipPositionMode.POINT)
                .unionFormat(
                        "function() {return '" + getString(R.string.count) + ": ' + this.points[0].value }");

        vertical.interactivity().hoverMode(HoverMode.BY_X);
        vertical.xAxis(true);
        vertical.yAxis(true);
        vertical.yAxis(0).labels().format("{%Value}");

        anyChartView.setChart(vertical);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }
}

