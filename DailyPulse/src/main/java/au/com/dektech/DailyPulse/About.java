package au.com.dektech.DailyPulse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class About extends AppCompatActivity {

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userLocalStore = new UserLocalStore(this);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.dailypulse);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView textView = (TextView) findViewById(R.id.textview_about_1);
        textView.setText(Html.fromHtml(getString(R.string.about_app)));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_account) {
            Log.v("AboutActivity", "DailyPulse: menu : Result button — navigating to the account page now!");
            Intent intent = new Intent(this, Account.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_show_results) {
            Log.d("AboutActivity", "DailyPulse: menu : Result button — navigating to the result page now!");
            Intent intent = new Intent(this, Result.class);
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
