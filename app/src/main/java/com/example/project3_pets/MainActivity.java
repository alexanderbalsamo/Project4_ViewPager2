package com.example.project3_pets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Balsamo";

    //preference stuff
    SharedPreferences myPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    //JSON info and connectivity check
    ConnectivityCheck myCheck;
    private String userURL;
    private JSONArray jsonArray;
    private int jsonNumArray;
    private String imageURL;

    // ViewPager2 object and adapter
    ViewPager2 vp;
    ViewPager2_Adapter csa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Toolbar and remove title
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Preference Change Listener
        myPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Listen for change to listPref key
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("listPref")){
                //Update URL and spinner
                getPrefValues(myPreferences);
                downloadURL();
//                try {
//                    setImage(jsonArray.getJSONObject(0).getString("file"));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
            }
        };

        // Register the listener
        myPreferences.registerOnSharedPreferenceChangeListener(listener);

        //Grab Preferences
        getPrefValues(myPreferences);

        //Download Images
        downloadURL();

        //set image
//        try {
//            setImage(jsonArray.getJSONObject(0).getString("file"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        //get a ref to the viewpager
        vp=findViewById(R.id.view_pager);
        //create an instance of the swipe adapter
        csa = new ViewPager2_Adapter(this);
        //set this viewpager to the adapter
        vp.setAdapter(csa);
    }

    private void setImage(String fileName) {
        String jsonPets = "pets.json";
        imageURL = userURL.substring(0, userURL.length()-jsonPets.length()) + fileName;
        WebImageView_KP imView = (WebImageView_KP) findViewById(R.id.imageView);
        imView.setImageUrl(imageURL);
        findViewById(R.id.imageView).setVisibility(View.VISIBLE);
    }

    private void getPrefValues(SharedPreferences settings) {
        userURL = settings.getString("listPref","https://www.pcs.cnu.edu/~kperkins/pets/pets.json");
    }

    public void processJSON(String string) {
        try {
            JSONObject jsonobject = new JSONObject(string);

            // you must know what the data format is, a bit brittle
            jsonArray = jsonobject.getJSONArray("pets");

            // pass the jsonArray and the URL to the viewPager
            csa.passJSONInfo(jsonArray, userURL);

            //DEBUG
            //Log.d(TAG, jsonArray.toString());

            // how many entries
            //jsonNumArray = jsonArray.length();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void downloadURL() {
        // Clear Existing Arrays
        jsonArray = null;
        jsonNumArray = 0;
        //Create new connectivity check instance
        myCheck = new ConnectivityCheck(this);
        boolean network = myCheck.isNetworkReachable();
        boolean wifi = myCheck.isWifiReachable();
        if (network || wifi) {
            DownloadTask_KP myTask = new DownloadTask_KP(this);
            myTask.execute(userURL);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this,SettingsActivity.class);
            startActivity(settingsIntent);
        }

        //all else fails let super handle it
        return super.onOptionsItemSelected(item);
    }
}

