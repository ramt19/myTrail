package com.wip.major.mytrail;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;

public class LocationSMS {
    String phone;
    String msg;

    public LocationSMS(String phone, String msg){
        this.phone = phone;
        this.msg = msg;
    }


    protected void checkPermission(){

//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
//
//        }
    }

    protected void sendSMS(){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone, null, msg, null, null);
    }
}
