package com.example.project3_pets;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Download_Image_Task extends AsyncTask<String, Void, Bitmap> {
    private static final String     TAG = "ImageDownloadTask";
    private static final int        DEFAULTBUFFERSIZE = 50;
    private static final int        NODATA = -1;
    private int                     statusCode=0;
    private String                  url;

    // get reference to my viewpager
    ViewPager2_Adapter myViewPager2;

    /**
     *
     * @param params  just the single url of the site to download from
     * @return null failed
     *         otherwise a bitmap
     */

    @Override
    protected Bitmap doInBackground(String... params) {
        // site we want to connect to
        url = params[0];

        // note streams are left willy-nilly here because it declutters the
        // example
        try {
            URL url1 = new URL(url);

            // this does no network IO
            HttpURLConnection connection = (HttpURLConnection) url1.openConnection();

            // can? instead of setImageResource? further configure connection before getting data
            // cannot do this after connected
            // connection.setRequestMethod("GET");
            // connection.setReadTimeout(timeoutMillis);
            // connection.setConnectTimeout(timeoutMillis);

            // this opens a connection, then sends GET & headers
            connection.connect();

            // lets see what we got make sure its one of
            // the 200 codes (there can be 100 of them
            // http_status / 100 != 2 does integer div any 200 code will = 2
            statusCode = connection.getResponseCode();

            if (statusCode / 100 != 2) {
                Log.e(TAG, "Error-connection.getResponseCode returned "
                        + Integer.toString(statusCode));
                return null;
            }

            // get our streams, a more concise implementation is
            // BufferedInputStream bis = new
            // BufferedInputStream(connection.getInputStream());
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            // the following buffer will grow as needed
            ByteArrayOutputStream baf = new ByteArrayOutputStream(DEFAULTBUFFERSIZE);
            int current = 0;

            // wrap in finally so that stream bis is sure to close
            try {
                while ((current = bis.read()) != NODATA) {
                    baf.write((byte) current);
                }

                // convert to a bitmap
                byte[] imageData = baf.toByteArray();
                return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            } finally {
                // close resource no matter what exception occurs
                bis.close();
            }
        } catch (Exception exc) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        ViewPager2_Adapter.getBitmap(result);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onCancelled()
     */
    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}