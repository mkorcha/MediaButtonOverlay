package com.mikekorcha.mediabuttonoverlay.players;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.mikekorcha.mediabuttonoverlay.MediaPlayer;

public class PlayMusic extends MediaPlayer {

    private static BroadcastReceiver statusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("playing", false)) {
                playStatus = PLAYING;

                return;
            }

            playStatus = PAUSED;
        }

    };

    private static IntentFilter statusFilter = new IntentFilter("com.android.music.playstatechanged");

    public PlayMusic(Context context) {
        super(context, statusReceiver, statusFilter);

        playerPackage = "com.google.android.music";
    }

    @Override
    public void sendNext() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "next");

        context.sendBroadcast(i);
    }

    @Override
    public void sendPrev() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "previous");

        context.sendBroadcast(i);
    }

    @Override
    public void sendPlay() {
        Intent i = new Intent("com.android.music.musicservicecommand.togglepause");

        context.sendBroadcast(i);
    }

    @Override
    public void sendPause() {
        sendPlay();
    }

}
