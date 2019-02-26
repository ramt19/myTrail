package com.wip.major.mytrail;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class LocationTracking extends Service {

    private SharedPreferences preferences;
    private String userID;
    private static final String TAG = "Location Tracking";
    private LocationManager mLocationManager = null;
    private Location mLocationGPS;
    private Location mLocationNET;
    private static final int LOCATION_INTERVAL = 0;
    private static final float LOCATION_DISTANCE = 0;
    private class LocationListener implements android.location.LocationListener{

        Location mLastLocation;
        public LocationListener(String provider){
            mLastLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location) {
            mLocationManager.removeUpdates(this);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(),"Enable"+provider.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    public LocationTracking() {
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)};

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        {

            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
                if(mLocationManager != null){
                    Log.i(TAG, "locationManagerGPS");
                    mLocationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(mLocationGPS != null){
                        Log.i(TAG, "location");
                        Log.i(TAG, String.valueOf(mLocationGPS.getLatitude()));
                        Log.i(TAG, String.valueOf(mLocationGPS.getLongitude()));
                        Log.i(TAG, String.valueOf(mLocationGPS));
                    }
                }

            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            }
        }
        if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[1]);
                if(mLocationManager != null) {
                    Log.i(TAG, "locationManagerNETWORK");
                    mLocationNET = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(mLocationGPS != null){
                        Log.i(TAG, "location");
                        Log.i(TAG, String.valueOf(mLocationNET.getLatitude()));
                        Log.i(TAG, String.valueOf(mLocationNET.getLongitude()));
                        Log.i(TAG, String.valueOf(mLocationNET));
                    }
                }
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "network provider does not exist, " + ex.getMessage());
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userID = preferences.getString("uid", "null");

        initializeLocationManager();


    }

    private void initializeLocationManager() {
        if(mLocationManager == null){
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
