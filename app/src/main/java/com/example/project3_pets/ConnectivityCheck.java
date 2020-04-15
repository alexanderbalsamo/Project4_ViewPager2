package com.example.project3_pets;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityCheck {
    Context context;

    ConnectivityCheck(Context context) {
        this.context = context;
    }

    public boolean isNetworkReachable() {
        ConnectivityManager mManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo current = mManager.getActiveNetworkInfo();
        if (current == null) {
            return false;
        }
        return (current.getState() == NetworkInfo.State.CONNECTED);
    }

    public boolean isWifiReachable() {
        ConnectivityManager mManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo current = mManager.getActiveNetworkInfo();
        if (current == null) {
            return false;
        }
        return (current.getType() == ConnectivityManager.TYPE_WIFI);
    }
}

