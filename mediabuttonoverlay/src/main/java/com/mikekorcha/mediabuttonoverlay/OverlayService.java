package com.mikekorcha.mediabuttonoverlay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class OverlayService extends Service {
    private static OverlayService      parent;

    private WindowManager              wm;
    private MediaControlView           mcv;

    private SharedPreferences          prefs;

    private WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    );

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        parent = this;

        this.wm   = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        this.mcv  = new MediaControlView(this);

        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

        this.prefs.edit().putBoolean("started", true).commit();

        this.wm.addView(this.mcv, this.params);

        this.Refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.prefs.edit().putBoolean("started", false).commit();

        if(this.mcv != null) {
            this.wm.removeView(this.mcv);
        }
    }

    public static class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                parent.Refresh();
            }
            catch(Exception e) {}
        }
    }

    private void Refresh() {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(prefs.getString("location", null).equals("Left")) {
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        }

        if(prefs.getString("location", null).equals("Right")) {
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        }

        this.mcv.setOpacity((float) prefs.getInt("opacity", 0) / 100);

        this.wm.updateViewLayout(this.mcv, this.params);
    }

    public void OnPrevClick(View v) {
        Intent i = new Intent();

        if(this.prefs.getString("player", null).equals("Google Play")) {
            i.setAction("com.android.music.musicservicecommand");
            i.putExtra("command", "previous");
            this.sendOrderedBroadcast(i, null);
        }

        if(this.prefs.getString("player", null).equals("Spotify")) {
            i.setAction("com.spotify.mobile.android.ui.widget.PREVIOUS");
            this.sendOrderedBroadcast(i, null);
        }

        if(this.prefs.getString("player", null).equals("Default Music Player")) {
            i = new Intent(Intent.ACTION_MEDIA_BUTTON);

            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            this.sendOrderedBroadcast(i, null);

            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            this.sendOrderedBroadcast(i, null);
        }
    }

    public void OnPPClick(View v) {
        Intent i = new Intent();

        if(this.prefs.getString("player", null).equals("Google Play")) {
            i.setAction("com.android.music.musicservicecommand.togglepause");
            this.sendOrderedBroadcast(i, null);
        }

        if(this.prefs.getString("player", null).equals("Spotify")) {
            i.setAction("com.spotify.mobile.android.ui.widget.PLAY");
            this.sendOrderedBroadcast(i, null);
        }

        if(this.prefs.getString("player", null).equals("Default Music Player")) {
            i = new Intent(Intent.ACTION_MEDIA_BUTTON);

            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
            this.sendOrderedBroadcast(i, null);

            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
            this.sendOrderedBroadcast(i, null);
        }
    }

    public void OnNextClick(View v) {
        Intent i = new Intent();

        if(this.prefs.getString("player", null).equals("Google Play")) {
            i.setAction("com.android.music.musicservicecommand");
            i.putExtra("command", "next");
            this.sendOrderedBroadcast(i, null);
        }

        if(this.prefs.getString("player", null).equals("Spotify")) {
            i.setAction("com.spotify.mobile.android.ui.widget.NEXT");
            this.sendOrderedBroadcast(i, null);
        }

        if(this.prefs.getString("player", null).equals("Default Music Player")) {
            i = new Intent(Intent.ACTION_MEDIA_BUTTON);

            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
            this.sendOrderedBroadcast(i, null);

            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
            this.sendOrderedBroadcast(i, null);
        }
    }

    public void OnCloseClick(View v) {
        this.prefs.edit().putBoolean("started", false).commit();

        this.stopSelf();
    }

}
