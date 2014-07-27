package com.mikekorcha.mediabuttonoverlay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class OverlayService extends Service {
    private static OverlayService      parent;

    private WindowManager              wm;
    private MediaControlView           mcv;

    private SharedPreferences          prefs;

    private Vibrator vibe;

    private WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    );

    public static Window win;

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

        this.vibe = (Vibrator) this.getBaseContext().getSystemService(Context.VIBRATOR_SERVICE); //this.mcv.getContext().getSystemService(Context.VIBRATOR_SERVICE);

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
        this.softVibrate();
        this.flash(this.mcv.prev);

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
        this.softVibrate();
        this.flash(this.mcv.pp);

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
        this.softVibrate();
        this.flash(this.mcv.next);

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
        this.softVibrate();

        this.prefs.edit().putBoolean("started", false).commit();

        this.stopSelf();
    }

    private void softVibrate() {
        if(this.vibe.hasVibrator() && this.prefs.getBoolean("vibrate", false)) {
            this.vibe.vibrate(50);
        }
    }

    public void flash(View v) {
        if(this.prefs.getBoolean("lighten", false)) {
            v.setAlpha((float) 1.0);
        }

        Timer t = new Timer();
        t.schedule(new EndFlash(), 200);

    }

    class EndFlash extends TimerTask {

        @Override
        public void run() {
            Intent i = new Intent();
            i.setAction("com.mikekorcha.mediabuttonoverlay.REFRESH");
            parent.sendOrderedBroadcast(i, null);
        }
    }
}
