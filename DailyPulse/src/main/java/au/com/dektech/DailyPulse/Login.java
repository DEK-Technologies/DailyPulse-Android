package au.com.dektech.DailyPulse;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.celpax.urlsign.URLSigner;
import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Login extends AppCompatActivity implements View.OnClickListener {

    EditText etUsername, etPassword;
    Button bLogin;

    TextView tvCreate, tvForgot;

    ProgressDialog prgDialog;

    UserLocalStore userLocalStore;

    String URL_REGISTER, SECRET_ACCESS_KEY, ACCESS_KEY_ID, URL_GET_SITES, URL_API_ENDPOINT,
            URL_SUBMISSION_REQUEST, URL_WEST_ENDPOINT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Config config = new Config();
        String[] credentials = config.configCredentials(getApplicationContext());

        if (credentials.length == 7) {
            URL_REGISTER = credentials[0];
            SECRET_ACCESS_KEY = credentials[1];
            ACCESS_KEY_ID = credentials[2];
            URL_GET_SITES = credentials[3];
            URL_API_ENDPOINT = credentials[4];
            URL_WEST_ENDPOINT = credentials[5];
            URL_SUBMISSION_REQUEST = credentials[6];

        } else {
            Log.e("LoginActivity", "DailyPulse Error in extracting credentials from the file");
        }

        setContentView(R.layout.activity_login);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etUsername.setHint("<Your business email address>");
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        tvCreate = (TextView) findViewById(R.id.tvCreate);
        tvForgot = (TextView) findViewById(R.id.tvForgot);

        bLogin.setOnClickListener(this);
        tvCreate.setOnClickListener(this);
        tvForgot.setOnClickListener(this);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        prgDialog.setCanceledOnTouchOutside(true);
        prgDialog.setMessage("Please wait...");

        userLocalStore = new UserLocalStore(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvCreate:
            case R.id.tvForgot:
                Uri uriUrl = Uri.parse(URL_REGISTER);
                Intent intentWebLogin = new Intent();
                intentWebLogin.setAction(Intent.ACTION_VIEW);
                intentWebLogin.addCategory(Intent.CATEGORY_BROWSABLE);
                intentWebLogin.setData(uriUrl);
                startActivity(intentWebLogin);
                break;
            case R.id.bLogin:
                Log.i("LoginActivity", "DailyPulse.onClick(): bLogin - clicked! Step 1");

                // Get Email Edit View Value
                String email = etUsername.getText().toString().replaceAll("\\s+$", "");
                // Get Password Edit View Value
                String password = etPassword.getText().toString();

                // When Email Edit View and Password Edit View have values other than Null
                if (Utility.isNotNull(email) && Utility.isNotNull(password)) {
                    // When Email entered is Valid
                    if (Utility.validate(email)) {
                        // Invoke RESTful Web Service with Http parameters

                        User user = new User(email, password);
                        //userLocalStore.storeUserData(user);
                        userLocalStore.storeUserDataInString(email, password);

                        prgDialog.show();

                        WSLogin wsLogin = new WSLogin();
                        wsLogin.invokeWSLogin(getApplicationContext());
                        //invokeWSLogin(getApplicationContext());

                        Log.i("LoginActivity", "DailyPulse.onClick(): bLogin - userLocalStore.getUserLogInStatus(): " + userLocalStore.getUserLogInStatus() /*+
                                ", wsLogin.isLogginSuccessful(): " + wsLogin.getLogginStatus()*/);

                        if (userLocalStore.getUserLogInStatus() == true /*|| wsLogin.getLoginStatus()*/) {
                            invokeWSGetSites();
                        }
                    }
                    // When Email is invalid
                    else {
                        Toast.makeText(getApplicationContext(), "Please enter a valid email address"
                                , Toast.LENGTH_LONG).show();
                    }
                }
                // When any of the Edit View control left blank
                else {
                    Toast.makeText(getApplicationContext(), "Please fill in the fields!",
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    protected void invokeWSLogin(Context context) {

        // Show Progress Dialog
        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        User user = userLocalStore.getLoggedInUser();
        params.put("j_username", user.username);
        params.put("j_password", user.password);
        params.put("remember_me", "false");

        Log.i("LoginActivity", "DailyPulse.onClick(): bLogin - clicked! user.username: " + user.username + ", user.password: " + user.password);

        int DEFAULT_TIMEOUT = 5000;
        client.setTimeout(DEFAULT_TIMEOUT);

        client.post(URL_WEST_ENDPOINT + URL_SUBMISSION_REQUEST, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                userLocalStore.setUserLogInStatus(true);

                Log.i("LoginActivity", "DailyPulse.onClick(): bLogin - clicked! Success response from the server: " + statusCode);
                Log.i("LoginActivity", "DailyPulse.onClick(): response.toString is:\t" + response.toString());
                try {
                    String submissionId = (response.getString("submitionId"));
                    String defaultSiteId = (response.getString("siteId"));

                    userLocalStore.setSubmissionDetails(submissionId, defaultSiteId);
                    invokeWSGetSites();
                    Toast.makeText(getApplicationContext(), "You are successfully logged in!", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Login success, BUT an error occurred [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    Log.e("LoginActivity", "DailyPulse.onClick(): bLogin - clicked! Error Occurred [Server's JSON response might be invalid: " + e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, java.lang.String errorResponse, java.lang.Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                // Hide Progress Dialog
                prgDialog.hide();
                Log.i("LoginActivity", "DailyPulse.onClick(): Step 6c - response is:\t" + errorResponse);
                Log.e("LoginActivity", "DailyPulse.onClick(): bLogin - clicked! Error code in response from the server: " + statusCode);

                // When Http response code is '401'
                if (statusCode == 401) {
                    Toast.makeText(getApplicationContext(), "Authentication Failed: Bad credentials\nTry again!", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '404'
                else if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured with code " + statusCode + ". [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                    Log.e("LoginActivity", "Unexpected Error occcured with code " + statusCode + ". [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // Hide Progress Dialog
                prgDialog.hide();
                Log.i("LoginActivity", "DailyPulse.onClick(): Step 6c - response is:\t" + errorResponse);
                Log.e("LoginActivity", "DailyPulse.onClick(): bLogin - clicked! Error code in response from the server: " + statusCode);

                Toast.makeText(getApplicationContext(), "Unexpected Error occurred with code " + statusCode + ". [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                Log.e("LoginActivity", "Unexpected Error occurred with code " + statusCode + ". [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
            }
        });
    }

    protected void invokeWSGetSites() {
        AsyncHttpClient client = new AsyncHttpClient();

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
                // Hide Progress Dialog
                prgDialog.hide();
                Log.i("LoginActivity", "DailyPulse - Success " +
                        "response from the server: " + statusCode + "\nand response is:\t" +
                        response.toString());

                Intent resultIntent = new Intent(getApplicationContext(), DeclareSite.class);

                userLocalStore.setSiteIDs(response);

                resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(resultIntent);
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable
                    e, org.json.JSONObject errorResponse) {
                // Hide Progress Dialog
                prgDialog.hide();
                Log.w("ResultActivity", "DailyPulse.onClick(): OnFailure - clicked! Error code in response from the server: "
                        + statusCode + "\n, errorResponse:\t" + errorResponse);

                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at the server's end", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected error occurred with code " + statusCode + ". [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                    Log.e("ResultActivity", "Unexpected Error occurred with code " + statusCode + ".");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable
                    throwable, JSONArray errorResponse) {
                prgDialog.hide();
                Log.w("LoginActivity", "DailyPulse - OnFailure: Error code in response from the server: "
                        + statusCode + "\n, errorResponse:\t" + errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String
                    responseString, Throwable throwable) {
                prgDialog.hide();
                Log.w("LoginActivity", "DailyPulse - OnFailure: Error code in response from the server: "
                        + statusCode + "\n, errorResponse:\t" + responseString);
                super.onFailure(statusCode, headers, responseString, throwable);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                prgDialog.hide();
                super.onSuccess(statusCode, headers, responseString);
            }
        });
    }
}
