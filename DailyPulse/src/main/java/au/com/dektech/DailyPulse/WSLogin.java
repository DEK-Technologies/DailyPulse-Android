package au.com.dektech.DailyPulse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.celpax.urlsign.URLSigner;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Alireza on 2016-04-18.
 */
public class WSLogin extends Activity{

    private Context context;
    public WSLogin (Context context) {
        this.context = context;
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     */
    protected void invokeWSLogin() {

        final UserLocalStore userLocalStore = new UserLocalStore(context);

        User user = userLocalStore.getLoggedInUser();

        if(user.password.equals("") || user.username.equals("")) {
            Log.e("WSLogin", "DailyPulse.onClick(): bLogin - clicked! user.password: " + user.password + ", user.username: " + user.username);
        }

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("j_username", user.username);
        params.put("j_password", user.password);
        params.put("remember_me", "false");

        String URL_WEST_ENDPOINT = userLocalStore.getUrlWestEndpoint();
        String URL_SUBMISSION_REQUEST = userLocalStore.getUrlSubmissionRequest();

        int DEFAULT_TIMEOUT = 5000;
        client.setTimeout(DEFAULT_TIMEOUT);

        client.post(URL_WEST_ENDPOINT + URL_SUBMISSION_REQUEST, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                userLocalStore.setUserLogInStatus(true);

                Log.i("WSLogin", "DailyPulse.onClick(): bLogin - clicked! now the getLoggedInUser() : " + userLocalStore.getLoggedInUser());

                Log.i("WSLogin", "DailyPulse.onClick(): bLogin - clicked! Success response from the server: " + statusCode);
                Log.i("WSLogin", "DailyPulse.onClick(): response.toString is:\t" + response.toString());
                try {
                    String submissionId = (response.getString("submitionId"));
                    String defaultSiteId = (response.getString("siteId"));

                    userLocalStore.setSubmissionDetails(submissionId, defaultSiteId);
                    Toast.makeText(context, "Authentication was successful!", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(context, "Login success, BUT an error occurred [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    Log.e("WSLogin", "DailyPulse.onClick(): bLogin - clicked! Error Occurred [Server's JSON response might be invalid: " + e.toString());
                    e.printStackTrace();
                }
                invokeWSGetSites();
                return;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, java.lang.String errorResponse, java.lang.Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i("WSLogin", "DailyPulse.onClick(): Step 6c - response is:\t" + errorResponse);
                Log.e("WSLogin", "DailyPulse.onClick(): bLogin - clicked! Error code in response from the server: " + statusCode);

                // When Http response code is '401'
                if (statusCode == 401) {
                    Toast.makeText(context, "Authentication Failed: Bad credentials\nTry again!", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '404'
                else if (statusCode == 404) {
                    Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(context, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(context, "Unexpected Error occcured with code " + statusCode + ". [Most common Error: Device might be disconnected from the Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                    Log.e("WSLogin", "Unexpected Error occcured with code " + statusCode + ". [Most common Error: Device might might be disconnected from the Internet or remote server is not up and running]");
                }
                return;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("WSLogin", "DailyPulse.onClick(): Step 6c - response is:\t" + errorResponse);
                Log.e("WSLogin", "DailyPulse.onClick(): bLogin - clicked! Error code in response from the server: " + statusCode);

                Toast.makeText(context, "Unexpected Error occurred with code " + statusCode + ". [Most common Error: Device might be disconnected from the Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                Log.e("WSLogin", "Unexpected Error occurred with code " + statusCode + ". [Most common Error: Device might be disconnected from the Internet or remote server is not up and running]");
                return;
            }
        });
        return;
    }

    protected void invokeWSGetSites() {
        AsyncHttpClient client = new AsyncHttpClient();

        final UserLocalStore userLocalStore = new UserLocalStore(context);

        String URL_GET_SITES = userLocalStore.getUrlGetSites();
        String SECRET_ACCESS_KEY = userLocalStore.getSecretAccessKey();
        String  ACCESS_KEY_ID = userLocalStore.getAccessKeyId();
        String URL_API_ENDPOINT = userLocalStore.getUrlApiEndpoint();

        String signature_mood;
        try {
            signature_mood = URLSigner.sign(URL_GET_SITES, SECRET_ACCESS_KEY);
            Log.v("LoginActivity", "DailyPulse.GetMoodKpi signature result: " + signature_mood);
        } catch (Exception e) {
            Log.e("LoginActivity", "DailyPulse: signing problem!" + e.toString());
            return;
        }

        client.addHeader("X-Celpax-Access-Key-Id", ACCESS_KEY_ID);
        client.addHeader("X-Celpax-Signature", signature_mood);

        client.get(URL_API_ENDPOINT + URL_GET_SITES, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.i("LoginActivity", "DailyPulse - Success " +
                        "response from the server: " + statusCode + "\nand response is:\t" +
                        response.toString());

                userLocalStore.setSiteIDs(response);

                navigateToDeclareSiteActivity();
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable
                    e, org.json.JSONObject errorResponse) {
                Log.w("ResultActivity", "DailyPulse.onClick(): OnFailure - clicked! Error code in response from the server: "
                        + statusCode + "\n, errorResponse:\t" + errorResponse);

                if (statusCode == 404) {
                    Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(context, "Something went wrong at the server's end", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Unexpected error occurred with code " + statusCode + ". [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                    Log.e("ResultActivity", "Unexpected Error occurred with code " + statusCode + ".");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable
                    throwable, JSONArray errorResponse) {
                /*prgDialog.hide();*/
                Log.w("LoginActivity", "DailyPulse - OnFailure: Error code in response from the server: "
                        + statusCode + "\n, errorResponse:\t" + errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String
                    responseString, Throwable throwable) {
                /*prgDialog.hide();*/
                Log.w("LoginActivity", "DailyPulse - OnFailure: Error code in response from the server: "
                        + statusCode + "\n, errorResponse:\t" + responseString);
                super.onFailure(statusCode, headers, responseString, throwable);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                /*prgDialog.hide();*/
                super.onSuccess(statusCode, headers, responseString);
            }
        });
    }

    /**
     * Method which navigates from Login Activity to Home Activity
     */
    public void navigateToDeclareSiteActivity() {
        Intent intent = new Intent(context, DeclareSite.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
