
package listviewitems;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import au.com.dektech.DailyPulse.R;
import au.com.dektech.DailyPulse.UserLocalStore;

public class PieChartItem extends ChartItem {

    private SpannableString mCenterText;

    public PieChartItem(ChartData<?> cd, Context c) {
        super(cd);

        mCenterText = generateCenterText();
    }
    UserLocalStore userLocalStore;

    @Override
    public int getItemType() {
        return TYPE_PIECHART;
    }

    @Override
    public View getView(int position, View convertView, final Context c) {

        ViewHolder holder = null;

        userLocalStore = new UserLocalStore(c);

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(c).inflate(
                    R.layout.list_item_piechart, null);
            holder.chart = (PieChart) convertView.findViewById(R.id.chart);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.chart.setDescription(userLocalStore.getUserCategory() + " on " +
                userLocalStore.getResultFetchDate() + " - KPI results of the last 30 days!");

        holder.chart.setRotationEnabled(true);
        holder.chart.setHighlightPerTapEnabled(true);

        holder.chart.setHoleRadius(63f);
        holder.chart.setTransparentCircleRadius(61f);
        holder.chart.setCenterText(mCenterText);
/*        holder.chart.setCenterTextTypeface(mTf);*/
        holder.chart.setCenterTextSize(9f);
        holder.chart.setUsePercentValues(true);
        holder.chart.setExtraOffsets(5, 10, 50, 10);
        holder.chart.setDrawCenterText(true);

        mChartData.setValueFormatter(new PercentFormatter());
        mChartData.setValueTextSize(11f);
        mChartData.setValueTextColor(Color.DKGRAY);
        // set data
        holder.chart.setData((PieData) mChartData);

        Legend l = holder.chart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        holder.chart.setTransparentCircleColor(Color.WHITE);

        holder.chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        holder.chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {
                if (entry == null)
                    return;
                final String[] xData = {"Happy", "Unhappy"};
                Toast.makeText(c, entry.getVal() + "%" + " of " + userLocalStore.getUserCategory() + " are " + xData[entry.getXIndex()]
                        , Toast.LENGTH_SHORT).show();
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

    private SpannableString generateCenterText() {
        // Preparing the text inside the pie chart
        SpannableString s = new SpannableString(
                "Daily Pulse Client\ndeveloped by DEK Technologies Sweden");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 19, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 19, s.length() - 23, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 19, s.length() - 23, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 19, s.length() - 23, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 23, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 23, s.length(), 0);
        return s;
    }

    private static class ViewHolder {
        PieChart chart;
    }
}
