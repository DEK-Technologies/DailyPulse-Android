package au.com.dektech.DailyPulse;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class to handle user credentials
 * It fetches from a private file the information necessary to submit votes, etc:
 * Strings:     URL_REGISTER, SECRET_ACCESS_KEY, ACCESS_KEY_ID, PANEL_ID
 */
public class Config {
    UserLocalStore userLocalStore;

    public String[] configCredentials(Context context) {

        String json;
        try {

            userLocalStore = new UserLocalStore(context);

            AssetManager manager = context.getAssets();
            InputStream is = manager.open("credentials.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            String SECRET_ACCESS_KEY, ACCESS_KEY_ID, PANEL_ID_SWEDEN_MOBILE, PANEL_ID_SWEDEN_DEK,
                    PANEL_ID_VIETNAM_MOBILE, PANEL_ID_VIETNAM_DEK, URL_API_ENDPOINT, URL_WEST_ENDPOINT, URL_REGISTER,
                    URL_GET_SITES, URL_SUBMISSION_REQUEST, URL_VOTE, URL_MOOD_KPI;

            JSONObject jsonObject = new JSONObject(json);

            SECRET_ACCESS_KEY = jsonObject.getString("SECRET_ACCESS_KEY");
            ACCESS_KEY_ID = jsonObject.getString("ACCESS_KEY_ID");
            PANEL_ID_SWEDEN_MOBILE = jsonObject.getString("PANEL_ID_SWEDEN_MOBILE");
            PANEL_ID_SWEDEN_DEK = jsonObject.getString("PANEL_ID_SWEDEN_DEK");
            PANEL_ID_VIETNAM_DEK = jsonObject.getString("PANEL_ID_VIETNAM_DEK");
            PANEL_ID_VIETNAM_MOBILE = jsonObject.getString("PANEL_ID_VIETNAM_MOBILE");

            URL_API_ENDPOINT = jsonObject.getString("URL_API_ENDPOINT");
            URL_WEST_ENDPOINT = jsonObject.getString("URL_WEST_ENDPOINT");
            URL_REGISTER = jsonObject.getString("URL_REGISTER");
            URL_GET_SITES = jsonObject.getString("URL_GET_SITES");
            URL_SUBMISSION_REQUEST = jsonObject.getString("URL_SUBMISSION_REQUEST");
            URL_VOTE = jsonObject.getString("URL_VOTE");
            URL_MOOD_KPI = jsonObject.getString("URL_MOOD_KPI");

            /*Log.v("config.java", "DailyPulse SECRET_ACCESS_KEY is: " + SECRET_ACCESS_KEY +
                    ", ACCESS_KEY_ID: " + ACCESS_KEY_ID + ", PANEL_ID_SWEDEN_MOBILE: " + PANEL_ID_SWEDEN_MOBILE +
                    ", PANEL_ID_VIETNAM: " + PANEL_ID_VIETNAM + ", URL_API_ENDPOINT: "
                    + URL_API_ENDPOINT + ", URL_REGISTER: " + URL_REGISTER + ", URL_GET_SITES: "
                    + URL_GET_SITES + ", URL_SUBMISSION_REQUEST: " + URL_SUBMISSION_REQUEST +
                    ", URL_VOTE: " + URL_VOTE + ", URL_MOOD_KPI: " + URL_MOOD_KPI);*/

            if ((SECRET_ACCESS_KEY != null || ACCESS_KEY_ID != null || PANEL_ID_SWEDEN_MOBILE
                    != null || PANEL_ID_SWEDEN_DEK != null || PANEL_ID_VIETNAM_DEK != null
                    || PANEL_ID_VIETNAM_MOBILE != null || URL_API_ENDPOINT != null
                    || URL_WEST_ENDPOINT != null || URL_REGISTER != null || URL_GET_SITES != null
                    || URL_SUBMISSION_REQUEST != null || URL_VOTE != null || URL_MOOD_KPI != null)){

                userLocalStore.setCredentials(
                        SECRET_ACCESS_KEY,
                        ACCESS_KEY_ID,
                        PANEL_ID_SWEDEN_MOBILE,
                        PANEL_ID_SWEDEN_DEK,
                        PANEL_ID_VIETNAM_DEK,
                        PANEL_ID_VIETNAM_MOBILE,

                        URL_API_ENDPOINT,
                        URL_WEST_ENDPOINT,
                        URL_REGISTER,
                        URL_GET_SITES,
                        URL_SUBMISSION_REQUEST,
                        URL_VOTE,
                        URL_MOOD_KPI);

                String[] response = {URL_REGISTER, SECRET_ACCESS_KEY, ACCESS_KEY_ID, URL_GET_SITES,
                        URL_API_ENDPOINT, URL_WEST_ENDPOINT, URL_SUBMISSION_REQUEST};
                return response;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("config.java", "DailyPulse error reading the credentials.json file!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("config.java", "DailyPulse error reading the credentials.json file - " +
                    "some error occurred!");
        }

        String[] response = {""};
        return response;
    }
}
