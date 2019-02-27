package com.wip.major.mytrail;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.InetAddress;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationSMS {
    private String uid;
    private String lat;
    private String lon;
    private DatabaseReference firebaseDatabase;
    private Context context;
    private static final String TAG = LocationSMS.class.getSimpleName();
    private static ArrayList<String> list = null;

    public LocationSMS(Context context, String lat, String lon){
        this.lat = lat;
        this.lon = lon;
        this.context = context;
    }


    protected void sendSMS() {
        if (isNetworkConnected()) {
            list = new ArrayList<String>();
            list.clear();
            SharedPreferences pref = context.getSharedPreferences("session", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = pref.edit();
            uid = pref.getString("uid", "null");
            firebaseDatabase = FirebaseDatabase.getInstance().getReference();
            DatabaseReference users = firebaseDatabase.child("Users");
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(uid).child("Contacts").exists()){
                        editor.putString("contacts","true");
                    }
                    else
                    {
                        editor.putString("contacts","false");
                    }
                    editor.commit();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.child(uid).child("Contacts").getChildren()) {
                        String phone = (String) dataSnapshot1.getValue();
                        list.add(phone);
                        Log.i(TAG, phone);
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phone, null, genSMS(), null, null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            users.addListenerForSingleValueEvent(valueEventListener);
        }
        else {
            if (list != null) {
                for (String phone : list
                        ) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phone, null, genSMS(), null, null);
                }
            }
            else{
                Toast.makeText(context,"Connect to Internet", Toast.LENGTH_SHORT).show();
            }
        }

    }

    protected String genSMS(){
        String msg;
        msg = "https://www.google.com/maps/search/?api=1&query="+lat+","+lon;
        return msg;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
