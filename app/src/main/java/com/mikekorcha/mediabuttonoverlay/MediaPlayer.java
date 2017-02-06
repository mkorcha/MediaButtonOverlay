package com.mikekorcha.mediabuttonoverlay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.widget.Toast;

public abstract class MediaPlayer {

    protected Context context;

    protected static int playStatus = MediaPlayer.UNKNOWN;

    protected static String playerPackage;

    public static final int PLAYING = 0;
    public static final int PAUSED  = 1;
    public static final int UNKNOWN = 2;

    private BroadcastReceiver receiver;

    protected MediaPlayer(Context context) {
        this.context = context;
    }

    protected MediaPlayer(Context context, BroadcastReceiver statusReceiver, IntentFilter statusFilter) {
        this.context = context;

        if(statusReceiver != null && statusFilter != null && statusReceiver != receiver) {
            this.context.registerReceiver(statusReceiver, statusFilter);

            this.receiver = statusReceiver;
        }
    }

    public abstract void sendNext();

    public abstract void sendPrev();

    public abstract void sendPlay();

    public abstract void sendPause();

    public int getPlaybackStatus() {
        return playStatus;
    }

    public void launchPlayer() {
        if(playerPackage != null) {
            PackageManager pm = context.getPackageManager();

            try {
                // This will throw an exception if the package isn't installed, acting as a check
                // for apps not installed
                if(!pm.getApplicationInfo(playerPackage, 0).enabled) {
                    Toast.makeText(context, "Player is disabled.", Toast.LENGTH_SHORT).show();

                    return;
                }
            }
            catch(PackageManager.NameNotFoundException e) {
                e.printStackTrace();

                Toast.makeText(context, "Player not installed.", Toast.LENGTH_SHORT).show();

                return;
            }

            Intent i = pm.getLaunchIntentForPackage(playerPackage);
            i.addCategory(Intent.CATEGORY_LAUNCHER);

            context.startActivity(i);
        }
    }

    public void destroyReceiver() {
        if(this.receiver != null) {
            context.unregisterReceiver(receiver);

            receiver = null;
        }
    }

}
