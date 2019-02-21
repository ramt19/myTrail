package com.wip.major.mytrail;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import static android.content.Context.LOCATION_SERVICE;

public class StartTracking {

    private Context mContext;
    private String mEmail;
    private String mPass;
    private String mUID;
    private Intent mIntent;
    private static final int PERMISSIONS_REQUEST = 1;

    public StartTracking (Context context, String email, String pass, String uid){
        mContext = context;
        mEmail = email;
        mPass = pass;
        mUID = uid;
        checkPermissions();
    }

    private void checkPermissions() {
        LocationManager lm = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(mContext, "Please enable location services", Toast.LENGTH_SHORT).show();
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
    }

    private void startTrackerService() {
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putString("email", mEmail);
        myEditor.putString("pass", mPass);
        myEditor.putString("uid", mUID);
        myEditor.commit();
        mContext.startService(new Intent(mContext, RealTimeTracking.class));
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
//            grantResults) {
//        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            // Start the service when the permission is granted
//            startTrackerService();
//        } else {
//
//        }
//    }
}
