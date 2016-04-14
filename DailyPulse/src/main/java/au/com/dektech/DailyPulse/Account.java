package au.com.dektech.DailyPulse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Account extends AppCompatActivity {

    TextView tvUserSite;
    private Toolbar toolbar;
    private RadioGroup radioOfficeGroup;
    private RadioButton radioOfficeButton;

    private Button bSignOut, bResult;

    private ProgressDialog prgDialog;

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account);
        userLocalStore = new UserLocalStore(this);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.dailypulse);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        prgDialog = new ProgressDialog(this);
        prgDialog.setCanceledOnTouchOutside(true);
        prgDialog.setMessage("Please wait...");

        tvUserSite = (TextView) findViewById(R.id.tv_account_tv_site_description);

        bSignOut = (Button) findViewById(R.id.b_sign_out);

        bResult = (Button) findViewById(R.id.b_result);

        setUserInfoText();

        radioOfficeGroup = (RadioGroup) findViewById(R.id.rb_main_office_Or_customer_office);

        radioOfficeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.i("AccountActivity", "DailyPulse: menu : site Changed! + userLocalStore.getUserCategory(): " + userLocalStore.getUserCategory());
                int selectedOfficeOption = radioOfficeGroup.getCheckedRadioButtonId();
                radioOfficeButton = (RadioButton) findViewById(selectedOfficeOption);
                String officeType = (String) radioOfficeButton.getText();

                prgDialog.show();
                if (userLocalStore.getUserCategory().contains("Sweden")) {
                    Log.i("AccountActivity", "DailyPulse: menu : site Changed! + userLocalStore.getUserCategory().contains(Sweden)) + \nradioOfficeButton:" + radioOfficeButton);
                    if (officeType.equals("Main Office"))
                        userLocalStore.setUserCategory("DEK Sweden");
                    else if (officeType.equals("Customer's office"))
                        userLocalStore.setUserCategory("Sweden Mobile Employees");
                } else if (userLocalStore.getUserCategory().contains("Vietnam")) {
                    Log.i("AccountActivity", "DailyPulse: menu : site Changed! + userLocalStore.getUserCategory().contains(Vietnam))");
                    if (officeType.equals("Main Office"))
                        userLocalStore.setUserCategory("DEK Vietnam");
                    else if (officeType.equals("Customer's office"))
                        userLocalStore.setUserCategory("Vietnam Mobile Employees");
                }
                userLocalStore.setUserCategoryRecentlyChanged(true);
                prgDialog.hide();
                setUserInfoText();
                Log.i("AccountActivity", "DailyPulse: menu : site Changed! + userLocalStore.getUserCategory(): " + userLocalStore.getUserCategory());
                Toast.makeText(getApplicationContext(), "User category set to: " + userLocalStore.getUserCategory(), Toast.LENGTH_SHORT).show();
            }
        });

        bSignOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Toast.makeText(getApplicationContext(), "Signed out! \nAll user data erased", Toast.LENGTH_LONG).show();
                Log.i("AccountActivity", "DailyPulse: menu : sign out button — We are going to clear user data now!");
                userLocalStore.setUserLogInStatus(false);
                userLocalStore.clearUserData();
                navigateToLoginActivity();
            }
        });

        bResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                navigateToResultActivity();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        return true;
    }

    private void setUserInfoText() {
        tvUserSite.setText(getString(R.string.account_tv_site_description_str) + userLocalStore.getUserCategory());
    }

    /**
     * Method to navigate to the Login Activity
     */
    public void navigateToLoginActivity() {
        Intent resultIntent = new Intent(getApplicationContext(), Login.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(resultIntent);
    }

    /**
     * Method to navigate to the Result Activity
     */
    public void navigateToResultActivity() {
        Intent resultIntent = new Intent(getApplicationContext(), Result.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(resultIntent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_show_results) {
            Log.v("AccountActivity", "DailyPulse: menu: navigating to the result " +
                    "page now!");
            Intent intent = new Intent(this, Result.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_show_about) {
            Log.v("AccountActivity", "DailyPulse: menu: navigating to the about " +
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
