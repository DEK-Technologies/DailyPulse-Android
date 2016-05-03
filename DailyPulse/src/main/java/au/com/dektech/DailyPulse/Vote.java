package au.com.dektech.DailyPulse;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.celpax.urlsign.URLSigner;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class Vote extends AppCompatActivity {

    private static Context context;

    ImageButton imageButtonHappy, imageButtonSad;

    UserLocalStore userLocalStore;

    ProgressDialog prgDialog;

    Button bResult, bGetSubmissionId;

    String URL_VOTE, URL_API_ENDPOINT, ACCESS_KEY_ID, PANEL_ID, SECRET_ACCESS_KEY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        bResult = (Button) findViewById(R.id.b_result);
        bGetSubmissionId = (Button) findViewById(R.id.b_get_submission_id_again);
        bGetSubmissionId.setVisibility(View.GONE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.dailypulse);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        imageButtonHappy = (ImageButton) findViewById(R.id.happyImageButton);
        imageButtonSad = (ImageButton) findViewById(R.id.sadImageButton);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);

        // Set Cancelable as true
        prgDialog.setCanceledOnTouchOutside(true);

        userLocalStore = new UserLocalStore(this);

        SECRET_ACCESS_KEY = userLocalStore.getSecretAccessKey();
        ACCESS_KEY_ID = userLocalStore.getAccessKeyId();
        //TODO: get the panel ID related to the site that the user belongs to?
        PANEL_ID = userLocalStore.getPanelId();
        URL_VOTE = userLocalStore.getUrlVote();
        URL_API_ENDPOINT = userLocalStore.getUrlApiEndpoint();

        imageButtonHappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Set Progress Dialog Text
                prgDialog.setMessage("Submitting mood...");
                JSONArray votesArray = userLocalStore.prepareVote(1);
                Log.d("VoteActivity", "DailyPulse.onClick(): Happy vote - chosen! prepared voteArray: " + votesArray);
                prgDialog.show();
                invokeWSSubmitVotes(votesArray);
            }
        });

        //TODO: add a parameter to see if
        imageButtonSad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                prgDialog.setMessage("Submitting mood...");
                JSONArray voteArray = userLocalStore.prepareVote(0);
                Log.d("VoteActivity", "DailyPulse.onClick(): Sad vote - chosen! prepared voteArray: " + voteArray);
                prgDialog.show();
                invokeWSSubmitVotes(voteArray);
            }
        });

        Vote.context = getAppContext();

        bResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                navigateToResultActivity();
            }
        });
    }

    public static Context getAppContext() {
        return Vote.context;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authenticate() == false) {
            Toast.makeText(getApplicationContext(), "Please first login!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        } else if (userLocalStore.getUserCategory().equals("")) {
            Toast.makeText(getApplicationContext(), "Please choose your site first!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, DeclareSite.class);
            startActivity(intent);
        }
    }

    /**
     * Checks if user is logged in before showing the vote view.
     *
     * @return
     */
    private boolean authenticate() {
        return userLocalStore.getUserLogInStatus();
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param votesArray Array of today's votes submitted by the user
     */
    protected void invokeWSSubmitVotes(final JSONArray votesArray) {

        AsyncHttpClient client = new AsyncHttpClient();

        JSONObject jsonParams = new JSONObject();

        String signature_vote;
        StringEntity entity;

        try {
            signature_vote = URLSigner.sign(URL_VOTE, SECRET_ACCESS_KEY);
            Log.d("VoteActivity", "DailyPulse.vote signature result: " + signature_vote);

            /*jsonParams.put("panelId", PANEL_ID);*/
            jsonParams.put("submitionId", userLocalStore.getSubmissionId());
            jsonParams.put("votesArray", votesArray);
            entity = new StringEntity(jsonParams.toString());
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        } catch (JSONException jsonError) {
            Log.e("VoteActivity", "DailyPulse: Json problem!" + jsonError.toString());
            return;
        } catch (Exception e) {
            Log.e("VoteActivity", "DailyPulse: signing problem!" + e.toString());
            return;
        }

        Log.d("VoteActivity", "DailyPulse.onClick(): userLocalStore.getSubmissionId() to be sent: " + userLocalStore.getSubmissionId());

        client.addHeader("X-Celpax-Access-Key-Id", ACCESS_KEY_ID);
        client.addHeader("X-Celpax-Signature", signature_vote);

        int DEFAULT_TIMEOUT = 3000;
        client.setTimeout(DEFAULT_TIMEOUT);

        client.post(context, URL_API_ENDPOINT + URL_VOTE, entity, "application/json", new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                prgDialog.hide();
                try {
                    JSONObject responseAsJsonObject = new JSONObject(new String(responseBody));
                    Log.w("VoteActivity", "DailyPulse.onClick(): onFailure response.toString is:\t"
                            + responseAsJsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("VoteActivity", "DailyPulse.onClick(): onFailure bLogin - clicked! Response from the server: " + statusCode);
                // Save this vote that failed to be sent so it would be sent with next vote
                userLocalStore.saveLastUnsuccessfulVoteArray(votesArray.toString());
                Toast.makeText(getApplicationContext(), "Error!\nCould not submit the vote!", Toast.LENGTH_SHORT).show();
                enableGettingSubmissionId();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                prgDialog.hide();
                userLocalStore.removeLastUnsuccessfulVoteArray();
                Toast.makeText(getApplicationContext(), "Thank you!\nVote successfully submitted" +
                        "\nas " + userLocalStore.getUserCategory() + "!", Toast.LENGTH_SHORT).show();
                Log.i("VoteActivity", "DailyPulse.onClick(): bVote - clicked! onSuccess response" +
                        " from the server: " + statusCode);
                for (Header header : headers) {
                    Log.d("VoteActivity", "DailyPulse.onClick(): onSuccess header:\t" +
                            (header.getName() + ", value: " + header.getValue()) + "\n");
                }
                navigateToResultActivity();
            }
        });
    }

    private void enableGettingSubmissionId() {
        bGetSubmissionId.setVisibility(View.VISIBLE);
        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        bGetSubmissionId.startAnimation(animation);
        bGetSubmissionId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.clearAnimation();
                WSLogin wsLogin = new WSLogin(getApplicationContext());
                wsLogin.invokeWSLogin();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        //// getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_account) {
            Log.v("VoteActivity", "DailyPulse: menu : Result button — navigating to the account page now!");
            Intent intent = new Intent(this, Account.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_show_results) {
            Log.v("VoteActivity", "DailyPulse: menu : Result button — navigating to the result page now!");
            Intent intent = new Intent(this, Result.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_show_about) {
            Log.v("VoteActivity", "DailyPulse: menu: Result button — navigating to the about page now!");
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method which navigates from Login Activity to Home Activity
     */
    public void navigateToResultActivity() {
        Intent resultIntent = new Intent(getApplicationContext(), Result.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(resultIntent);
    }

}
