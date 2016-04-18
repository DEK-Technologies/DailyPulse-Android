package au.com.dektech.DailyPulse;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Class to handle app data
 * It stores and maintains user information:
 * Strings:     USERNAME, PASSWORD, submissionId, siteId
 * booleans     LOGGED_IN,
 */
public class UserLocalStore {

    public static final String SharedPreference_NAME = "userDetails";
    SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context) {
        /* Param 0 translates to MODE_PRIVATE rather than MODE_WORLD_READABLE
        or MODE_WORLD_WRITEABLE */
        userLocalDatabase = context.getSharedPreferences(SharedPreference_NAME, 0);
    }

    /**
     * Stores user data upon the first time the user logs in successfully.
     *
     * @param user
     */
    public void storeUserData(User user) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("USERNAME", user.username);
        spEditor.putString("PASSWORD", user.password);
        spEditor.commit();
    }

    public void setCredentials(String secret_access_key, String access_key_id,
                               String PANEL_ID_SWEDEN_MOBILE, String PANEL_ID_SWEDEN_DEK,
                               String PANEL_ID_VIETNAM_DEK, String PANEL_ID_VIETNAM_MOBILE,
                               String url_api_endpoint, String url_west_endpoint,
                               String url_register, String url_get_sites,
                               String url_submission_request, String url_vote,
                               String url_mood_kpi) {

        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("SECRET_ACCESS_KEY", secret_access_key);
        spEditor.putString("ACCESS_KEY_ID", access_key_id);
        spEditor.putString("PANEL_ID_SWEDEN_MOBILE", PANEL_ID_SWEDEN_MOBILE);
        spEditor.putString("PANEL_ID_SWEDEN_DEK", PANEL_ID_SWEDEN_DEK);
        spEditor.putString("PANEL_ID_VIETNAM_DEK", PANEL_ID_VIETNAM_DEK);
        spEditor.putString("PANEL_ID_VIETNAM_MOBILE", PANEL_ID_VIETNAM_MOBILE);

        spEditor.putString("URL_API_ENDPOINT", url_api_endpoint);
        spEditor.putString("URL_WEST_ENDPOINT", url_west_endpoint);
        spEditor.putString("URL_REGISTER", url_register);
        spEditor.putString("URL_GET_SITES", url_get_sites);
        spEditor.putString("URL_SUBMISSION_REQUEST", url_submission_request);
        spEditor.putString("URL_VOTE", url_vote);
        spEditor.putString("URL_MOOD_KPI", url_mood_kpi);
        spEditor.commit();

    }

    public User getLoggedInUser() {
        String username = userLocalDatabase.getString("USERNAME", "");
        String password = userLocalDatabase.getString("PASSWORD", "");

        User storedUser = new User(username, password);

        return storedUser;
    }

    public void setUserLogInStatus(boolean logged_in_flag) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("LOGGED_IN", logged_in_flag);
        spEditor.putString("LAST_VOTE_DATE", "");
        spEditor.putString("LAST_RESULT_FETCHING_DATE", "");
        spEditor.commit();
    }

    public void setSubmissionDetails(String submission_id, String defaultSiteId) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("SUBMISSION_ID", submission_id);
        spEditor.putString("DEFAULT_SITE_ID", defaultSiteId);
        spEditor.commit();
    }

    public String getSubmissionId() {
        return userLocalDatabase.getString("SUBMISSION_ID", "");
    }

    public String getSecretAccessKey() {
        return userLocalDatabase.getString("SECRET_ACCESS_KEY", "");
    }

    public String getAccessKeyId() {
        return userLocalDatabase.getString("ACCESS_KEY_ID", "");
    }

    public String getDefaultSiteId() {
        return userLocalDatabase.getString("DEFAULT_SITE_ID", "");
    }

    /**
     * Returns the panelID specific to the user's site.
     * @return
     */
    public String getPanelId() {
        String userPanel = "";
        if (getUserCategory().equals("DEK Vietnam")) {
            userPanel = "PANEL_ID_VIETNAM_DEK";
        } else if (getUserCategory().equals("Vietnam Mobile Employees")) {
            userPanel = "PANEL_ID_VIETNAM_MOBILE";
        } else if (getUserCategory().equals("DEK Sweden")) {
            userPanel = "PANEL_ID_SWEDEN_DEK";
        } else if (getUserCategory().equals("Sweden Mobile Employees")) {
            userPanel = "PANEL_ID_SWEDEN_MOBILE";
        }
        return userLocalDatabase.getString(userPanel, "");

    }

    public String getUrlGetSites() {
        return userLocalDatabase.getString("URL_GET_SITES", "");
    }

    public String getUrlRegister() {
        return userLocalDatabase.getString("URL_REGISTER", "");
    }

    public String getUrlApiEndpoint() {
        return userLocalDatabase.getString("URL_API_ENDPOINT", "");
    }

    public String getUrlWestEndpoint() {
        return userLocalDatabase.getString("URL_WEST_ENDPOINT", "");
    }

    public String getUrlSubmissionRequest() {
        return userLocalDatabase.getString("URL_SUBMISSION_REQUEST", "");
    }

    public String getUrlVote() {
        return userLocalDatabase.getString("URL_VOTE", "");
    }

    public String getUrlMoodKpi() {
        return userLocalDatabase.getString("URL_MOOD_KPI", "");
    }

    /**
     * @param v 0 if red, and 1 if green
     */
    public JSONArray prepareVote(int v) {
        // votesArray = [{vote: 1, submitionDate: utc_timestamp,            dayDuplicated : false},
        //               {vote: 0, submitionDate: utc_timestamp - 35000000, dayDuplicated : true} ];
        //TODO: if first vote of the date, an array of one jsonObject;
        // if not a first-time voter, append to the previous jsonObject

        JSONArray votesArray = new JSONArray();
        JSONObject voteObj = new JSONObject();

        //TODO: IF the unsuccessfulVoteArray is not empty, append the current incoming vote and send back the array
        try {
            voteObj.put("vote", v);
            voteObj.put("submitionDate", System.currentTimeMillis());

            Log.d("userLocalStore", "DailyPulse.prepareVote() - LAST_VOTE_DATE: " +
                    (userLocalDatabase.getString("LAST_VOTE_DATE", "")) + ", & dateRightNow: " +
                    (com.celpax.urlsign.URLSigner.getFormattedDate()));
            if ((userLocalDatabase.getString("LAST_VOTE_DATE", "")).
                    equals(com.celpax.urlsign.URLSigner.getFormattedDate())) {
                Log.i("userLocalStore", "DailyPulse.perpareVote() - LAST_VOTE_DATE This is not " +
                        "the user's first vote today! However vote will be prepared to be sent!");
                voteObj.put("dayDuplicated", true);
            } else {
                Log.i("userLocalStore", "DailyPulse.prepareVote() - LAST_VOTE_DATE This seems to " +
                        "be the user's first vote of the day!");
                voteObj.put("dayDuplicated", false);
            }
        } catch (JSONException e) {
            Log.e("UserLocalStore", "Exception thrown while trying to make the JsonArray of " +
                    "votes to be submitted(): " + e.toString());
        }

        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        // TODO: if successful, do not save the vote, and empty the unsuccessfulVoteArray; if voteFailedToBeSubmitted, append it to the unsuccessfulVoteArray.
        Log.d("userLocalStore", "DailyPulse compare --> result of userLocalDatabase.getString" +
                "(\"votesArrayInString\", \"\").equals(\"\") is :::: " +
                userLocalDatabase.getString("votesArrayInString", "").equals(""));

        votesArray.put(voteObj);

        if (!(userLocalDatabase.getString("votesArrayInString", "").equals(""))) {
            String previouslySavedArray = userLocalDatabase.getString("votesArrayInString", "");
            Log.i("userLocalStore", "DailyPulse.prepareVote(): There exists a votesArrayInString " +
                    "that is unsuccessful votes from the past: " + previouslySavedArray);
            try {
                JSONArray jsonArray = new JSONArray(previouslySavedArray);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    votesArray.put(row);
                }
            } catch (JSONException e) {
                Log.e("userLocalStore", "DailyPulse.prepareVote(): Error while trying to fetch " +
                        "the awaiting jsonArray" + e.toString());
            }
            Log.i("userLocalStore", "DailyPulse.prepareVote(): The votesArrayInString that would " +
                    "be " +"sent now is: " + votesArray);
        }

        spEditor.commit();
        spEditor.putString("votesArrayInString", votesArray.toString());
        setLastVoteDate();
        Log.i("userLocalStore", "DailyPulse.prepareVote() - votesArray.toString(): "
                + votesArray.toString());
        return votesArray;
        // votesArray.put(object);
    }

    /**
     * Save the unsuccessful vote array that just failed to be sent
     *
     * @param lastFailedVoteArray
     */
    public void saveLastUnsuccessfulVoteArray(String lastFailedVoteArray) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        //TODO: get the current unsuccessful vote Array, convert to jsonArray and append to it, convert back to string and save it.
        spEditor.putString("votesArrayInString", lastFailedVoteArray);
        spEditor.commit();
    }

    /**
     * Removes the unsuccessful vote array that was waiting to be sent
     */
    public void removeLastUnsuccessfulVoteArray() {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.remove("votesArrayInString");
        spEditor.commit();
    }

    public void setLastVoteDate() {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("LAST_VOTE_DATE", com.celpax.urlsign.URLSigner.getFormattedDate());
        spEditor.commit();
    }

    public boolean hasVotedToday() {
        if ((userLocalDatabase.getString("LAST_VOTE_DATE", "")).
                equals(com.celpax.urlsign.URLSigner.getFormattedDate())) {
            return true;
        }
        return false;
    }

    public void setLastResultFetchingDate() {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("LAST_RESULT_FETCHING_DATE",
                com.celpax.urlsign.URLSigner.getFormattedDate());
        spEditor.commit();
    }

    public String getResultFetchDate() {
        return (userLocalDatabase.getString("LAST_RESULT_FETCHING_DATE", ""));
    }

    public boolean hasFetchedResultsToday() {

        Log.i("userLocalStore", "DailyPulse (userLocalDatabase.getString" +
                "(\"LAST_RESULT_FETCHING_DATE\", \"\")): " +
                (userLocalDatabase.getString("LAST_RESULT_FETCHING_DATE", "")) +
                ", com.celpax.urlsign.URLSigner.getFormattedDate()): " +
                com.celpax.urlsign.URLSigner.getFormattedDate());
        if ((userLocalDatabase.getString("LAST_RESULT_FETCHING_DATE", "")).
                equals(com.celpax.urlsign.URLSigner.getFormattedDate())) {
            return true;
        }
        return false;
    }

    public void setTodaysMoodKpiForAllSites(String[][] allResults) {

        JSONArray lastStoredJsonArray = new JSONArray();
        for (int i = 0; i < allResults.length; i++) {
            JSONObject siteResultObject = new JSONObject();

            try {

                siteResultObject.put("siteID", allResults[i][0]);
                siteResultObject.put("siteIdDescription", allResults[i][1]);
                siteResultObject.put("red", allResults[i][2]);
                siteResultObject.put("green", allResults[i][3]);
                siteResultObject.put("kpiDate", allResults[i][4]);

                lastStoredJsonArray.put(siteResultObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("MOOD_KPI_ALL_SITES", lastStoredJsonArray.toString());

        Log.v("userLocalStore", "DailyPulse Vote array to be saved in persistence memory is: " +
                lastStoredJsonArray.toString());
        spEditor.commit();
    }

    public String[][] getLastStoredMoodKpi() {

        String jsonArrayStr = userLocalDatabase.getString("MOOD_KPI_ALL_SITES", "");
        String[][] allResults = new String[getNumberOfSites()][5];

        if (jsonArrayStr.equals("")) {
            Log.w("userLocalStore", "DailyPulse: MOOD_KPI_ALL_SITES is not supposed to be empty!");
        }

        try {
            JSONArray allKpiResultsJsonArray = new JSONArray(jsonArrayStr);

            for (int i = 0; i < allKpiResultsJsonArray.length(); i++) {
                JSONObject allKpiResultsJsonObject = allKpiResultsJsonArray.getJSONObject(i);
                allResults[i][0] = allKpiResultsJsonObject.getString("siteID");
                allResults[i][1] = allKpiResultsJsonObject.getString("siteIdDescription");
                allResults[i][2] = allKpiResultsJsonObject.getString("red");
                allResults[i][3] = allKpiResultsJsonObject.getString("green");
                allResults[i][4] = allKpiResultsJsonObject.getString("kpiDate");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return allResults;
    }

    public void setAllSiteDescriptions(ArrayList<String> allSiteDescriptionsList) {
        String[] allSiteDescriptionsArray =
                allSiteDescriptionsList.toArray(new String[allSiteDescriptionsList.size()]);

        StringBuilder allSiteDescriptionsStringBuilder = new StringBuilder();
        for (int i = 0; i < allSiteDescriptionsArray.length; i++) {
            allSiteDescriptionsStringBuilder.append(allSiteDescriptionsArray[i]).append(",");
        }

        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("ALL_SITE_DESCRIPTIONS", allSiteDescriptionsStringBuilder.toString());
        spEditor.commit();
    }

    /**
     * Returns a String[] of site Descriptions
     * @return
     */
    public String[] getAllSiteDescriptions() {
        String[] allSitesDescriptions = userLocalDatabase.getString("ALL_SITE_DESCRIPTIONS", "").
                split(",");
        Log.v("userLocalStore", "DailyPulse: userLocalDatabase.getString" +
                "(\"ALL_SITE_DESCRIPTIONS\", \"\").split(\",\")" + allSitesDescriptions);
        return allSitesDescriptions;
    }

    public int getUserSiteIndex() {
        String jsonArrayStr = userLocalDatabase.getString("MOOD_KPI_ALL_SITES", "");

        if (jsonArrayStr.equals("")) {
            Log.w("userLocalStore", "DailyPulse: MOOD_KPI_ALL_SITES is not supposed to be empty!");
        }

        try {
            JSONArray allKpiResultsJsonArray = new JSONArray(jsonArrayStr);

            for (int i = 0; i < allKpiResultsJsonArray.length(); i++) {
                JSONObject allKpiResultsJsonObject = allKpiResultsJsonArray.getJSONObject(i);

                if (allKpiResultsJsonObject.getString("siteIdDescription").
                        equals(getUserCategory())) {

                    return i;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("userLocalStore", "DailyPulse: Not found any matchin UserCategory!");
        return -1;
    }


    public boolean getUserLogInStatus() {
        if (userLocalDatabase.getBoolean("LOGGED_IN", false) == true) {
            return true;
        } else {
            return false;
        }
    }

    public void clearUserData() {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.clear();
        spEditor.commit();
    }

    public void setSiteIDs(JSONArray siteIDs) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("SITE_IDS", siteIDs.toString());
        spEditor.putInt("NUMBER_OF_SITES", siteIDs.length());
        spEditor.commit();
    }

    public String getSiteIDs() {
        return (userLocalDatabase.getString("SITE_IDS", ""));
    }

    public int getNumberOfSites() {
        //TODO: When making the app generic, change the value of 4 below to -1
        return (userLocalDatabase.getInt("NUMBER_OF_SITES", 4));
    }

    public void setUserCategory(String userCategory) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("USER_CATEGORY", userCategory);
        setUserCategoryRecentlyChanged(true);
        spEditor.commit();
    }

    public String getUserCategory() {
        return (userLocalDatabase.getString("USER_CATEGORY", ""));
    }

    public void setUserCategoryRecentlyChanged(boolean value) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("USER_CATEGORY_RECENTLY_CHANGED", value);
        spEditor.commit();
    }

    public boolean getUserCategoryRecentlyChanged() {
        return (userLocalDatabase.getBoolean("USER_CATEGORY_RECENTLY_CHANGED", false));
    }
}
