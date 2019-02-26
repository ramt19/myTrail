package com.wip.major.mytrail;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;

public class LocationSMS {
    private String phone;
    private String lat;
    private String lon;

    public LocationSMS(String phone, String lat, String lon){
        this.phone = phone;
        this.lat = lat;
        this.lon = lon;
    }


    protected void sendSMS(){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone, null, genSMS(), null, null);
    }

    protected String genSMS(){
        String msg;
        msg = "https://www.google.com/maps/search/?api=1&query="+lat+","+lon;
        return msg;
    }
}
