package listviewitems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import au.com.dektech.DailyPulse.R;
import au.com.dektech.DailyPulse.UserLocalStore;

public class BarChartItem extends ChartItem {
    

    public BarChartItem(ChartData<?> cd, Context c) {
        super(cd);

    }

    @Override
    public int getItemType() {
        return TYPE_BARCHART;
    }

    UserLocalStore userLocalStore;

    @Override
    public View getView(int position, View convertView, final Context c) {

        userLocalStore = new UserLocalStore(c);

        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(c).inflate(
                    R.layout.list_item_barchart, null);
            holder.chart = (BarChart) convertView.findViewById(R.id.chart);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // apply styling
        holder.chart.setDescription("");
        holder.chart.setDrawGridBackground(false);
        holder.chart.setDrawBarShadow(false);

        XAxis xAxis = holder.chart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        
        YAxis leftAxis = holder.chart.getAxisLeft();
        leftAxis.setLabelCount(5, false);
        leftAxis.setSpaceTop(20f);
        mChartData.setValueTextSize(11f);
        leftAxis.setAxisMinValue(0f);
       
        YAxis rightAxis = holder.chart.getAxisRight();
        rightAxis.setLabelCount(5, false);
        rightAxis.setSpaceTop(20f);
        rightAxis.setAxisMinValue(0f);

        // set data
        holder.chart.setData((BarData) mChartData);
        
        // do not forget to refresh the chart
        holder.chart.invalidate();
        holder.chart.animateY(700);

        holder.chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {
                if (entry == null)
                    return;
                String[] allSitesDescriptions = userLocalStore.getAllSiteDescriptions();
                final String[] xData = allSitesDescriptions;
                Toast.makeText(c, entry.getVal() + "%" + " of " + xData[entry.getXIndex()] +
                        " have been happy during the past 30 days!", Toast.LENGTH_SHORT).show();
                if (entry.getVal() < 0) {
                    Toast.makeText(c, "Minus values show that there has been not enough " +
                            "data gathered about this site yet!", Toast.LENGTH_SHORT).show();
                }
                }

            @Override
            public void onNothingSelected() {
            }
        });

        return convertView;
    }
    
    private static class ViewHolder {
        BarChart chart;
    }
}
