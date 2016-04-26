package au.com.dektech.DailyPulse;

import android.app.ProgressDialog;
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
                        userLocalStore.storeUserDataInString(email, password);

                        //Create a Thread for this.
                        prgDialog.show();

                        WSLogin wsLogin = new WSLogin(getApplicationContext());

                        wsLogin.invokeWSLogin();

                        Log.i("LoginActivity", "DailyPulse.onClick(): bLogin - userLocalStore.getUserLogInStatus(): " + userLocalStore.getUserLogInStatus());
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
}
