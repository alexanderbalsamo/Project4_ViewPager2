package com.example.project3_pets;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Balsamo";

    SharedPreferences myPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    ConnectivityCheck myCheck;
    private String userURL;
    private JSONArray jsonArray;
    private int jsonNumArray;
    Spinner spinner;
    private String imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Toolbar and remove title
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Get Spinner
        spinner = (Spinner)findViewById(R.id.spinner);

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
                    try {
                        setImage(jsonArray.getJSONObject(0).getString("file"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        try {
            setImage(jsonArray.getJSONObject(0).getString("file"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setImage(String fileName) {
        String jsonPets = "pets.json";
        imageURL = userURL.substring(0, userURL.length()-jsonPets.length()) + fileName;
        WebImageView_KP imView = (WebImageView_KP) findViewById(R.id.imageView);
        imView.setImageUrl(imageURL);
        findViewById(R.id.imageView).setVisibility(View.VISIBLE);
    }

    public void errorAlert() {
        String alertMessage = "404 Error!";
        new AlertDialog.Builder(this)
                .setMessage(alertMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // This will do nothing
                    }
                })
                .show();
        // disable things
        spinner.setEnabled(false);
        spinner.setVisibility(View.GONE);
        findViewById(R.id.imageView).setVisibility(View.GONE);
    }

    private void getPrefValues(SharedPreferences settings) {
        userURL = settings.getString("listPref","https://www.pcs.cnu.edu/~kperkins/pets/pets.json");
    }

    public void processJSON(String string) {
        if (string == null) {
            errorAlert();
        }
        try {
            JSONObject jsonobject = new JSONObject(string);

            // you must know what the data format is, a bit brittle
            jsonArray = jsonobject.getJSONArray("pets");
            Log.d(TAG, jsonArray.toString());

            // how many entries
            jsonNumArray = jsonArray.length();

            // Populate spinner
            setupSimpleSpinner();

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
        else{
            errorAlert();
        }
    }

    private void setupSimpleSpinner() {
        // Turn on spinner
        spinner.setEnabled(true);
        spinner.setVisibility(View.VISIBLE);
        //create a data adapter to fill above spinner with choices
        // Loop through JSON to add to List
        List<String> petList = new ArrayList<>();
        for (int i = 0; i < jsonNumArray; i++){
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                petList.add(jsonObject.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        //bind the spinner to the datasource managed by adapter
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, petList));

        //respond when spinner clicked
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public static final int SELECTED_ITEM = 0;

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long rowid) {
                if (arg0.getChildAt(SELECTED_ITEM) != null) {
                    try {
                        setImage(jsonArray.getJSONObject(pos).getString("file"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
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

