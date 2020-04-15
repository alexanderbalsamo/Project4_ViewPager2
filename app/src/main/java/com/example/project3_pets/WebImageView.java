package com.example.project3_pets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.appcompat.widget.AppCompatImageView;;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class WebImageView extends AppCompatImageView {

    private static final long TRANISTION_MILLISECONDS = 150;
    protected Drawable mPlaceholder, mImage;

    public WebImageView(Context context, AttributeSet set) {
        super(context,set);
        initImageTransitionAnimations();
    }
    //context is required by parent not this class
    public WebImageView(Context context) {
        super(context);
        initImageTransitionAnimations();
    }

    /**
     * The big one go and download image represented by URL
     * @param url
     */
    public void setImageUrl(String url) {
        DownloadTask task = new DownloadTask();
        task.execute(url);
    }



    /**
     * An async task to download images
     * First param is the URL to download
     * Third param will be the bitmap returned, or null if failure
     *
     * Can probably be used to fetch binary data as well
     */
    private class DownloadTask extends AsyncTask<String, Void, Bitmap> {
        private static final String     TAG = "ImageDownloadTask";
        private static final int        DEFAULTBUFFERSIZE = 50;
        private static final int        NODATA = -1;
        private int                     statusCode=0;
        private String                  url;

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

                // can further configure connection before getting data
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
            if (result == null)
                return;

            //looks like it worked!
            mImage = new BitmapDrawable(result);
            if (mImage != null) {
                doTransitionAnimations();
            }
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
    };

    /**
     * default image to show if we cannot load desired one
     *
     * @param drawable
     */
    public void setPlaceholderImage(Drawable drawable) {
        // error check
        if (drawable != null) {
            mPlaceholder = drawable;
            if (mImage == null) {
                setImageDrawable(mPlaceholder);
            }
        }
    }

    /**
     * get default from resources
     *
     * @param resid
     */
    public void setPlaceholderImage(int resid) {
        mPlaceholder = getResources().getDrawable(resid);
        setImageDrawable(mPlaceholder);
    }

    //these are fadeout fadein animations to
    //smooth the harsh transition of switching between images
    Animation animation1;
    Animation animation2;
    private void initImageTransitionAnimations() {
        animation1 = new AlphaAnimation(1.0f, 0.0f);
        animation1.setDuration(TRANISTION_MILLISECONDS);

        animation2 = new AlphaAnimation(0.0f, 1.0f);
        animation2.setDuration(TRANISTION_MILLISECONDS);
        animation2.setStartOffset(TRANISTION_MILLISECONDS);
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setImageDrawable(mImage);
                startAnimation(animation2);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    private void doTransitionAnimations(){
        startAnimation(animation1);
    }
}

