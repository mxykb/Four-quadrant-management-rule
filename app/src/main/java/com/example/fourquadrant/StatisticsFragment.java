package com.example.fourquadrant;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {
    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        pieChart = view.findViewById(R.id.pie_chart);
        setupPieChart();
        return view;
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40f, "第一象限"));
        entries.add(new PieEntry(30f, "第二象限"));
        entries.add(new PieEntry(20f, "第三象限"));
        entries.add(new PieEntry(10f, "第四象限"));

        PieDataSet dataSet = new PieDataSet(entries, "任务分布");
        dataSet.setColors(new int[]{Color.RED, Color.YELLOW, Color.BLUE, Color.LTGRAY});
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(16f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(false);
        Description desc = new Description();
        desc.setText("");
        pieChart.setDescription(desc);
        pieChart.getLegend().setEnabled(true);
        pieChart.invalidate();

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    PieEntry pieEntry = (PieEntry) e;
                    Toast.makeText(getContext(), pieEntry.getLabel() + ": " + pieEntry.getValue() + "%", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected() {
                // do nothing
            }
        });
    }
} 