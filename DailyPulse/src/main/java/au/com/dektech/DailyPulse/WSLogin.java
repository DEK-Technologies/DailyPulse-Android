package au.com.dektech.DailyPulse;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Alireza on 2016-04-18.
 */
public class WSLogin {

    private boolean isLogginSuccessful;

    public WSLogin() {
        isLogginSuccessful = false;
    }

    public boolean getLoginStatus(){
        return isLogginSuccessful;
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param context
     */
    protected void invokeWSLogin(final Context context) {
  /*protected void invokeWSLogin(final Context context, String username, String password) {*/

        final UserLocalStore userLocalStore = new UserLocalStore(context);

        User user = userLocalStore.getLoggedInUser();
        String[] loggedInUserInStr = userLocalStore.getLoggedInUserInString();

        Log.v("WSLogin", "DailyPulse.onClick(): bLogin - clicked! user.password: " + user.password + ", user.username: " + user.username +
        "\nloggedInUserInStr[0]: " + loggedInUserInStr[0] + ", loggedInUserInStr[1]: " + loggedInUserInStr[1]);

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
                isLogginSuccessful = true;

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
                    Toast.makeText(context, "Unexpected Error occcured with code " + statusCode + ". [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                    Log.e("WSLogin", "Unexpected Error occcured with code " + statusCode + ". [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
                }
                return;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("WSLogin", "DailyPulse.onClick(): Step 6c - response is:\t" + errorResponse);
                Log.e("WSLogin", "DailyPulse.onClick(): bLogin - clicked! Error code in response from the server: " + statusCode);

                Toast.makeText(context, "Unexpected Error occurred with code " + statusCode + ". [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                Log.e("WSLogin", "Unexpected Error occurred with code " + statusCode + ". [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
                return;
            }
        });
        return;
    }
}
