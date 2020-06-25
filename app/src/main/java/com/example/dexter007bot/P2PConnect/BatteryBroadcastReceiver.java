package com.example.dexter007bot.P2PConnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import static com.example.dexter007bot.LoginActivity.logger;

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    int prevLevel;

    public BatteryBroadcastReceiver() {
        prevLevel = -1;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        if (prevLevel != level) {
            logger.write(String.valueOf(level));
            prevLevel = level;
        } else
            Log.d("Battery_P2P", "Same battery Level");
    }
}
