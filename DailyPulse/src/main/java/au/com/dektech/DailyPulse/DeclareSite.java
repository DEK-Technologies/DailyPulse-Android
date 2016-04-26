package au.com.dektech.DailyPulse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class DeclareSite extends AppCompatActivity implements View.OnClickListener {

    Button bSubmit;
    private RadioGroup radioCountryId, radioOfficeId;
    private RadioButton radioCountryButton, radioOfficeButton;

    ProgressDialog prgDialog;

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_declaration);
        userLocalStore = new UserLocalStore(this);

        if (!userLocalStore.getUserCategory().equals("")) {
            Intent intent = new Intent(this, Result.class);
            finish();
            startActivity(intent);
        }

        bSubmit = (Button) findViewById(R.id.bSubmit);

        bSubmit.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        prgDialog = new ProgressDialog(this);
        prgDialog.setCanceledOnTouchOutside(true);
        prgDialog.setMessage("Submitting mood...");
        prgDialog.show();

        Log.i("DeclareSiteActivity", "DailyPulse.onClick(): bLogin - clicked! Step 1");

        //TODO: Make below generic once API version 2 is out.
        radioCountryId = (RadioGroup) findViewById(R.id.radioCountry);
        radioOfficeId = (RadioGroup) findViewById(R.id.radioOffice);
        int selectedCountryOption = radioCountryId.getCheckedRadioButtonId();
        int selectedOfficeOption = radioOfficeId.getCheckedRadioButtonId();
        radioCountryButton = (RadioButton) findViewById(selectedCountryOption);
        radioOfficeButton = (RadioButton) findViewById(selectedOfficeOption);
        String countryType = (String) radioCountryButton.getText();
        String officeType = (String) radioOfficeButton.getText();

        if (countryType.equals("Vietnam")) {
            if (officeType.equals("DEK's office")) {
                userLocalStore.setUserCategory("DEK Vietnam");
            } else userLocalStore.setUserCategory("Vietnam Mobile Employees");
        } else if (countryType.equals("Sweden")) {
            if (officeType.equals("DEK's office")) {
                userLocalStore.setUserCategory("DEK Sweden");
            } else userLocalStore.setUserCategory("Sweden Mobile Employees");
        }
        Log.i("DeclareSiteActivity", "DailyPulse.onClick(): userLocalStore.getUserCategory(): "
                + userLocalStore.getUserCategory());

        prgDialog.hide();

        if (userLocalStore.getUserLogInStatus() == true) {
            Intent intent = new Intent(this, Result.class);
            startActivity(intent);
        }
    }
}
