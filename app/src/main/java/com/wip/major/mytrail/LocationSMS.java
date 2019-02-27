package com.wip.major.mytrail;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LocationSMS {
    private String uid;
    private String lat;
    private String lon;
    private DatabaseReference firebaseDatabase;
    private Context context;
    private static final String TAG = LocationSMS.class.getSimpleName();

    public LocationSMS(Context context, String lat, String lon){
        this.lat = lat;
        this.lon = lon;
        this.context = context;
    }


    protected void sendSMS(){
        SharedPreferences pref = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        uid = pref.getString("uid","null");
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference users = firebaseDatabase.child("Users");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.child(uid).child("Contacts").getChildren()){
                    String phone = (String) dataSnapshot1.getValue();
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

    protected String genSMS(){
        String msg;
        msg = "https://www.google.com/maps/search/?api=1&query="+lat+","+lon;
        return msg;
    }
}
