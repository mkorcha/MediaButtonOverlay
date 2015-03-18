package com.mikekorcha.mediabuttonoverlay.players;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.mikekorcha.mediabuttonoverlay.MediaPlayer;

public class Default extends MediaPlayer {

    public Default(Context context) {
        super(context);
    }

    @Override
    public void sendNext() {
        sendKeycodeIntent(KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    @Override
    public void sendPrev() {
        sendKeycodeIntent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }

    @Override
    public void sendPlay() {
        sendKeycodeIntent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    @Override
    public void sendPause() {
        sendPlay();
    }

    private void sendKeycodeIntent(int keycode) {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);

        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keycode));
        context.sendBroadcast(i);

        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keycode));
        context.sendBroadcast(i);
    }
}
