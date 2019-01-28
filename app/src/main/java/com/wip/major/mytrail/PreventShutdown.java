package com.wip.major.mytrail;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.KEYGUARD_SERVICE;

public class PreventShutdown extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)){
            Toast.makeText(context,"success",Toast.LENGTH_SHORT).show();
            Log.i("mytrail","shutdown");

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                KeyguardManager km = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);

                if (km.isKeyguardSecure()) {

                }
            }
        }
    }
}
