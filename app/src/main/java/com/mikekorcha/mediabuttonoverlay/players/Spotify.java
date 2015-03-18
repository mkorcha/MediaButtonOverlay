package com.mikekorcha.mediabuttonoverlay.players;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.mikekorcha.mediabuttonoverlay.MediaPlayer;

public class Spotify extends MediaPlayer {

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

    private static IntentFilter statusFilter = new IntentFilter("com.spotify.music.playbackstatechanged");

    public Spotify(Context context) {
        super(context, statusReceiver, statusFilter);

        playerPackage = "com.spotify.music";
    }

    @Override
    public void sendNext() {
        Intent i = new Intent("com.spotify.mobile.android.ui.widget.NEXT");

        context.sendBroadcast(i);
    }

    @Override
    public void sendPrev() {
        Intent i = new Intent("com.spotify.mobile.android.ui.widget.PREVIOUS");

        context.sendBroadcast(i);
    }

    @Override
    public void sendPlay() {
        Intent i = new Intent("com.spotify.mobile.android.ui.widget.PLAY");

        context.sendBroadcast(i);
    }

    @Override
    public void sendPause() {
        sendPlay();
    }
}
