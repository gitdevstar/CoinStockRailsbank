package com.brian.stocks.xmt;

import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Area;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.ScaleStackMode;
import com.brian.stocks.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class XMTChartFragment extends Fragment {
    private static JSONArray chartAskData, chartBidData;
    private AnyChartView mXMTChartView;

    public XMTChartFragment() {
        // Required empty public constructor
    }

    public static XMTChartFragment newInstance(JSONObject data) {
        Log.d("xmt chart data", data.toString());
        XMTChartFragment fragment = new XMTChartFragment();
        try {
            chartAskData = data.getJSONArray("ask");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            chartBidData = data.getJSONArray("bid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_xmt_chart, container, false);
        mXMTChartView = view.findViewById(R.id.xmt_history_chart);
        drawStocksChart();

        return view;
    }

    private void drawStocksChart() {

        Cartesian areaChart = AnyChart.area();

        areaChart.animation(false)
                .tooltip(false)
                .xAxis(true)
                .yAxis(true)
                .yGrid(true)
                .background(false);
        areaChart.yScale().stackMode(ScaleStackMode.VALUE);
        List<DataEntry> seriesData = new ArrayList<>();
         int size = 0;
         if(chartAskData.length() > chartBidData.length())
             size = chartBidData.length();
         else size = chartAskData.length();

        for (int i = 0; i < size; i ++) {
            try {
                seriesData.add(new CustomDataEntry("Q"+i, chartAskData.getJSONObject(i).getDouble("price"), chartBidData.getJSONObject(i).getDouble("price") ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Data = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping series2Data = set.mapAs("{ x: 'x', value: 'value2' }");

        Area series1 = areaChart.area(series1Data);
//        series1.name("Americas");
        series1.stroke("0 #ee204d");
        series1.color("#ee204d");

        Area series2 = areaChart.area(series2Data);
        series2.stroke("0 #3AE57F");
        series2.color("#3AE57F");
//        areaChart.legend().enabled(true);
//        areaChart.legend().fontSize(13d);
//        areaChart.legend().padding(0d, 0d, 20d, 0d);

//        areaChart.xAxis(0).title(false);
//        areaChart.yAxis(0).title("Revenue (in Billons USD)");

//        areaChart.interactivity().hoverMode(HoverMode.BY_X);
//        areaChart.tooltip()
//                .valuePrefix("$")
//                .valuePostfix(" bln.")
//                .displayMode(TooltipDisplayMode.UNION);

        mXMTChartView.setChart(areaChart);
    }

    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value, Number value2) {
            super(x, value);
            setValue("value2", value2);
        }
    }
}
