package au.com.dektech.DailyPulse;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.celpax.urlsign.URLSigner;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import listviewitems.BarChartItem;
import listviewitems.ChartItem;
import listviewitems.PieChartItem;

/**
 * This is the activity that would show the latest diagrams and results.
 */
public class Result extends AppCompatActivity {

    UserLocalStore userLocalStore;
    private GoogleApiClient client;

    private Toolbar toolbar;

    ProgressDialog prgDialog;

    private String[][] allKpiResults;
    private ArrayList<String> allSiteDescriptions = new ArrayList<>();
    private static int internalIndexOfSiteToBeSaved, numberOfSitesFetched, indexOfUsersSite;

    String URL_MOOD_KPI, ACCESS_KEY_ID, SECRET_ACCESS_KEY, URL_API_ENDPOINT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userLocalStore = new UserLocalStore(this);

        //// TODO: ONLY HAVE IF AUTHENTICATE == false INSTEAD????
        if (authenticate() == false) {
            Toast.makeText(getApplicationContext(), "Please first Login!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, Login.class);
            finish();
            startActivity(intent);
        } else if (userLocalStore.getUserCategory().equals("")) {
            Toast.makeText(getApplicationContext(), "Please choose your site first!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, DeclareSite.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_result);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.dailypulse);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        URL_MOOD_KPI = userLocalStore.getUrlMoodKpi();
        ACCESS_KEY_ID = userLocalStore.getAccessKeyId();
        SECRET_ACCESS_KEY = userLocalStore.getSecretAccessKey();
        URL_API_ENDPOINT = userLocalStore.getUrlApiEndpoint();


        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Fetching results...");
        // Set Cancelable as true
        prgDialog.setCanceledOnTouchOutside(true);

        /////TODO:remove this: showPieChart((userLocalStore.getLastStoredMoodKpi())[3]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Checks if it is a working day.
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        boolean isWeekday = ((dow >= Calendar.MONDAY) && (dow <= Calendar.FRIDAY));

/*
        if (!lastStoredMoodKpi[2].equals(null)
                && lastStoredMoodKpi[2].equals(com.celpax.urlsign.URLSigner.getFormattedDate())){

        }
*/
        Log.v("ResultActivity", "DailyPulse: \n!userLocalStore.hasFetchedResultsToday(): " + !userLocalStore.hasFetchedResultsToday() +
                "\nuserLocalStore.hasVotedToday(): " + userLocalStore.hasVotedToday() +
                "\nuserLocalStore.getUserCategoryRecentlyChanged(): " + userLocalStore.getUserCategoryRecentlyChanged() +
                ",\n!isWeekday: " + !isWeekday);

        //User can see the result page only if:
        //      it is a weekend day, Or
        //      if it is a weekday:
        //          He must have already voted + we have not FetchedResults today yet + uase has not changed his category recently.

        if (!userLocalStore.hasFetchedResultsToday() &&
                userLocalStore.hasVotedToday() &&
                userLocalStore.getUserCategoryRecentlyChanged() || !isWeekday){

            //////TODO: CORRECT THIS TO GET RESULTS OF ALL SITES and input it to the datapie
            String siteIdsJsonString = userLocalStore.getSiteIDs();
            Log.v("ResultActivity", "DailyPulse: new JSONArray(new String(responseBody)) is:\t" +
                    siteIdsJsonString);

            numberOfSitesFetched = 0;

            try {
                JSONArray siteIDsJsonArray = new JSONArray(siteIdsJsonString);
                allKpiResults = new String[siteIDsJsonArray.length()][5];
                String userSite = userLocalStore.getUserCategory();

                for (int i = 0; i < siteIDsJsonArray.length(); i++) {

                    internalIndexOfSiteToBeSaved = i;
                    JSONObject siteIDJsonObject = siteIDsJsonArray.getJSONObject(i);

                    allKpiResults[internalIndexOfSiteToBeSaved][0] =
                            siteIDJsonObject.getString("id");

                    allKpiResults[internalIndexOfSiteToBeSaved][1] =
                            siteIDJsonObject.getString("description");

                    Log.v("ResultActivity", "DailyPulse: siteIDJsonObject.getString(\"description\"):\t" + siteIDJsonObject.getString("description")
                            + ", and userSite: " + userSite);

                    // Within all sites' information, save the index of user's site for pie chart.

                    Log.v("ResultActivity", "DailyPulse: onResume the siteId to be used is:\t" +
                            siteIDJsonObject.getString("id"));
                    Log.v("ResultActivity", "DailyPulse: allKpiResults[internalIndexOfSiteToBeSaved][1]:\t" +
                            allKpiResults[internalIndexOfSiteToBeSaved][1] + "\nThe allKpiResults[][] is:\n" + Arrays.deepToString(allKpiResults));

                    // Invoke RESTful Web Service with Http parameters
                    invokeWSGetResults(siteIDJsonObject.getString("id"));

                }

                Log.v("ResultActivity", "DailyPulse: allKpiResults 2 [internalIndexOfSiteToBeSaved][1]:\t" +
                        allKpiResults[internalIndexOfSiteToBeSaved][1] + "\nThe allKpiResults[][] is:\n" + Arrays.deepToString(allKpiResults));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (userLocalStore.hasFetchedResultsToday()) {
            Log.v("ResultActivity", "DailyPulse userLocalStore.hasFetchedResultsToday() has passed!");

            allKpiResults = userLocalStore.getLastStoredMoodKpi();

            showCharts();
        } else {
            Log.v("ResultActivity", "No need to fetch results this time!");
            navigateToVoteActivity();
        }
    }

    private void showCharts() {
        // Does the following either if just received info from the server,
        // or if user is viewing the result page again during the day or during weekend.

        Log.v("ResultActivity", "DailyPulse: showCharts hello");

        ArrayList<ChartItem> list = new ArrayList<>();
        ListView lv = (ListView) findViewById(R.id.listView1);

        list.add(new PieChartItem(generateDataPie(userLocalStore.getUserSiteIndex()), getApplicationContext()));

        list.add(new BarChartItem(generateDataBar(), getApplicationContext()));

        ChartDataAdapter cda = new ChartDataAdapter(getApplicationContext(), list);
        lv.setAdapter(cda);
    }

    /** adapter that can support different item types */
    private class ChartDataAdapter extends ArrayAdapter<ChartItem> {

        public ChartDataAdapter(Context context, List<ChartItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getView(position, convertView, getContext());
        }

        @Override
        public int getItemViewType(int position) {
            // return the views type
            return getItem(position).getItemType();
        }

        @Override
        public int getViewTypeCount() {
            return 2; // we have 3 different item-types
        }
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return
     */
    private PieData generateDataPie(int indexForUserGroup) {

        final String[] xData = {"Happy", "Unhappy"};

        Log.v("ResultActivity", "indexForUserGroup: " + indexForUserGroup);

        Log.v("ResultActivity", "DailyPulse.piechart _B, \n" +
                "allKpiResults[indexForUserGroup][0]: " + allKpiResults[indexForUserGroup][0] +
                ", \nand allKpiResults[indexForUserGroup][1]: " + allKpiResults[indexForUserGroup][1] +
                ", \nand allKpiResults[indexForUserGroup][2]: " + allKpiResults[indexForUserGroup][2] +
                ", \nand allKpiResults[indexForUserGroup][3]: " + allKpiResults[indexForUserGroup][3]);

        ArrayList<Entry> entries = new ArrayList<>();
        //////TODO:: CORRECT THIS TO PRINT THE RELATED SITE ONLY.
        //Add value for Happy
        entries.add(new Entry(Integer.parseInt(allKpiResults[indexForUserGroup][3]), 0));

        //Add value for Unhappy
        entries.add(new Entry(Integer.parseInt(allKpiResults[indexForUserGroup][2]), 1));

        PieDataSet d = new PieDataSet(entries, "");

        if ((Integer.parseInt(allKpiResults[indexForUserGroup][2])) < 0) {
            final int[] MY_COLORS = {Color.GRAY, Color.GRAY};
            d.setColors(MY_COLORS);
        } else {
            final int[] MY_COLORS = {Color.rgb(178, 255, 102), Color.rgb(255, 153, 153)};
            d.setColors(MY_COLORS);
        }
        // set the color

        d.setSliceSpace(3f);
        d.setSelectionShift(6f);

        PieData cd = new PieData(xData, d);
        return cd;
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return
     */
    private BarData generateDataBar() {

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> allSiteDescriptionsInShort = new ArrayList<>();

        allSiteDescriptions.clear();
        for (int i = 0; i < userLocalStore.getNumberOfSites(); i++) {
            entries.add(new BarEntry(Integer.parseInt(allKpiResults[i][3]), i));

            String regexRemoveDEK = "\\s*\\bDEK\\b\\s*";
            String regexRemoveEmployee = "\\s*\\bEmployees\\b\\s*";
            String regexRemoveMobile = "\\s*\\bMobile\\b\\s*";

            allSiteDescriptionsInShort.add(allKpiResults[i][1]
                    .replaceAll(regexRemoveDEK, "").replaceAll(regexRemoveEmployee, "")
                    .replaceAll(regexRemoveMobile, "Mob"));
            Log.v("ResultActivity", "DailyPulse: allSiteDescriptions is:\n" + allSiteDescriptionsInShort);

            allSiteDescriptions.add(allKpiResults[i][1]);
        }

        BarDataSet d = new BarDataSet(entries, "30-day KPI Results of Company's all user groups!");
        d.setBarSpacePercent(20f);
        d.setColors(ColorTemplate.JOYFUL_COLORS);
        d.setHighLightAlpha(255);

        Log.v("ResultActivity", "allSiteDescriptions: " + allSiteDescriptions);

        userLocalStore.setAllSiteDescriptions(allSiteDescriptions);

        BarData cd = new BarData(allSiteDescriptionsInShort, d);
        return cd;
    }

    protected void invokeWSGetResults(final String siteId) {

        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();

        int DEFAULT_TIMEOUT = 3000;
        client.setTimeout(DEFAULT_TIMEOUT);

        String signature_mood;
        try {
            signature_mood = URLSigner.sign(URL_MOOD_KPI + siteId, SECRET_ACCESS_KEY);
            Log.v("ResultActivity", "DailyPulse.GetMoodKpi signature result: " + signature_mood);
        } catch (Exception e) {
            Log.e("ResultActivity", "DailyPulse: signing problem!" + e.toString());
            return;
        }

        client.addHeader("X-Celpax-Access-Key-Id", ACCESS_KEY_ID);
        client.addHeader("X-Celpax-Signature", signature_mood);
        Log.v("ResultActivity", "DailyPulse.GetMoodKpi siteId is: " + siteId);

        client.get(URL_API_ENDPOINT + URL_MOOD_KPI + siteId, new JsonHttpResponseHandler() {

            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // Hide Progress Dialog
                prgDialog.hide();
                Log.i("ResultActivity", "DailyPulse - results Fetched Successfully for siteId: " +
                        siteId + "response code from the server: " + statusCode +
                        "\nand response is:\t" + response.toString());
                try {
                    // When the JSON response has keys 'red' or 'green'
                    if (response.getString("red") != null && response.getString("green") != null) {

                        String red = response.getString("red");
                        String green = response.getString("green");
                        String KpiDate = response.getString("date");

                        saveResult(red, green, KpiDate, siteId);

                        //String[] lastStoredMoodKpi = userLocalStore.getLastStoredMoodKpi();
                        Log.i("ResultActivity", "DailyPulse.onClick(): Mood KPI successfully " +
                                "fetched! \nRed is: " + red + ",\nand green is: " + green);

                        userLocalStore.setLastResultFetchingDate();

                    }
                    // Else display an error message
                    else {
                        Toast.makeText(getApplicationContext(), response.getString("error_msg"),
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occurred [Server's JSON " +
                            "response might be invalid]!", Toast.LENGTH_SHORT).show();
                    Log.e("ResultActivity", "DailyPulse.onClick(): bLogin - clicked! Error " +
                            "Occurred [Server's JSON response might be invalid: " + e.toString());
                    e.printStackTrace();
                }
            }

            // When response returned by REST has Http response code other than '200' (eg. "4XX")
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e,
                                  org.json.JSONObject errorResponse) {
                // Hide Progress Dialog
                prgDialog.hide();

                Log.w("ResultActivity", "DailyPulse.onClick(): OnFailure - clicked! Error code " +
                        "in response from the server: "
                        + statusCode + "\n, errorResponse:\t" + errorResponse);

                // Save into String data in case of failure too.
                saveResult("-50", "-50", "", siteId);

                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found",
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong at the server's end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Could not fetch results for " +
                            allKpiResults[internalIndexOfSiteToBeSaved][1], Toast.LENGTH_SHORT).show();
                    Log.e("ResultActivity", "Unexpected Error occurred with code " + statusCode);
                }
            }
        });
    }

    private void saveResult(String red, String green, String kpiDate, String siteID) {

        for (int i = 0; i < userLocalStore.getNumberOfSites(); i++) {

            if (siteID.equals(allKpiResults[i][0])) {

                if (!isNumeric(red) || !isNumeric(green)) {
                    red = "-50";
                    green = "-50";
                    Toast.makeText(getApplicationContext(),
                            "No KPI yet for some of the sites in your organization!",
                            Toast.LENGTH_LONG).show();
                }

                allKpiResults[i][2] = red;
                allKpiResults[i][3] = green;
                allKpiResults[i][4] = kpiDate;

                Log.v("ResultActivity", "saveResults(): \n internalIndexOfSiteToBeSaved: " + internalIndexOfSiteToBeSaved +
                        ", and String red, String green, String kpiDate: " + red + ", " + green + ", " + kpiDate + ", siteId is: " + siteID +
                        ", \nindexofUsersSite: " + indexOfUsersSite + ", numberOfSitesFetched: " + numberOfSitesFetched +
                        ", and Arrays.deepToSting(allKpiResults):\n" + Arrays.deepToString(allKpiResults) +
                        ", \nuserLocalStore.getNumberOfSites(): " + userLocalStore.getNumberOfSites() +
                "\n((numberOfSitesFetched+1) == userLocalStore.getNumberOfSites())" + ((numberOfSitesFetched+1) == userLocalStore.getNumberOfSites()) );

                // Check if all answers are collected from the server
                if(++numberOfSitesFetched == userLocalStore.getNumberOfSites()) {
                    userLocalStore.setTodaysMoodKpiForAllSites(allKpiResults);
                    showCharts();
                }
                return;
            }
        }
        return;
    }

    public static boolean isNumeric(String str)
    {
        try
        { Integer.parseInt(str); }
        catch(Exception e)
        { return false; }
        return true;
    }

    /**
     * Method which navigates from Login Activity to Home Activity
     */
    public void navigateToVoteActivity() {
        Intent resultIntent = new Intent(getApplicationContext(), Vote.class);
        Toast.makeText(getApplicationContext(),
                "Vote before you can see results!", Toast.LENGTH_LONG).show();
        finish();
        startActivity(resultIntent);
    }

    private boolean authenticate() {
        return userLocalStore.getUserLogInStatus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_account) {
            Log.v("ResultActivity", "DailyPulse: menu : Result button — navigating to the " +
                    "account page now!");
            Intent intent = new Intent(this, Account.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_show_results) {
            Log.v("ResultActivity", "DailyPulse: menu: Result button — navigating to the result " +
                    "page now!");
            Intent intent = new Intent(this, Result.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_show_about) {
            Log.v("ResultActivity", "DailyPulse: menu: Result button — navigating to the about " +
                    "page now!");
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_vote) {
            Log.v("ResultActivity", "DailyPulse: menu: Result button — navigating to the about " +
                    "page now!");
            Intent intent = new Intent(this, Vote.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
