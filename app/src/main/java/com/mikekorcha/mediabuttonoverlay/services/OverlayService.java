package com.mikekorcha.mediabuttonoverlay.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mikekorcha.mediabuttonoverlay.views.MediaOverlayView;
import com.mikekorcha.mediabuttonoverlay.MediaPlayer;
import com.mikekorcha.mediabuttonoverlay.R;
import com.mikekorcha.mediabuttonoverlay.views.OverlayDropView;

import java.lang.reflect.InvocationTargetException;

public class OverlayService extends Service implements MediaOverlayView.OnMediaButtonClickListeners {

    private static OverlayService that;

    private WindowManager windowManager;

    private Vibrator vibrator;

    private SharedPreferences sharedPrefs;

    private WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
    );

    private MediaPlayer mediaPlayer;
    private MediaOverlayView mediaOverlay;
    private OverlayDropView overlayDropView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        that = this;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        setMediaPlayer();

        mediaOverlay = new MediaOverlayView(this, getLayoutResource());
        mediaOverlay.setOnClickListeners(this);

        overlayDropView = new OverlayDropView(this);
        overlayDropView.setVisibility(View.INVISIBLE);

        windowManager.addView(overlayDropView, new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        ));

        windowManager.addView(mediaOverlay, layoutParams);

        refresh();

        if(sharedPrefs.getBoolean("startapp", false)) {
            mediaPlayer.launchPlayer();
        }
    }

    @Override
    public void onDestroy() {
        if(mediaOverlay != null) {
            windowManager.removeView(mediaOverlay);
        }

        if(overlayDropView != null) {
            windowManager.removeView(overlayDropView);
        }

        mediaPlayer.destroyReceiver();

        that = null;

        super.onDestroy();
    }

    @SuppressWarnings("unchecked")
    private void setMediaPlayer() {
        if(mediaPlayer != null) {
            mediaPlayer.destroyReceiver();
        }

        try {
            Class player = Class.forName(getPackageName() + ".players." + sharedPrefs.getString("player", "Default"));

            mediaPlayer = (MediaPlayer) player.getConstructor(Context.class).newInstance(this);
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException e) {
            toastyError("Media player not properly implemented", e);
        }
        catch (InstantiationException | InvocationTargetException e) {
            toastyError("Unable to instantiate controls for media player", e);
        }
    }

    private void refresh() {
        mediaOverlay.setLayout(getLayoutResource());

        mediaOverlay.setColour(sharedPrefs.getInt("colour", getResources().getColor(R.color.primary)));

        mediaOverlay.setOpacity(sharedPrefs.getFloat("opacity", 0.5f));

        mediaOverlay.setOrientation(sharedPrefs.getString("orientation", "0").equals("0") ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);

        if(sharedPrefs.getInt("location", MediaOverlayView.LEFT) == MediaOverlayView.LEFT) {
            layoutParams.gravity = Gravity.LEFT;
        }
        else {
            layoutParams.gravity = Gravity.RIGHT;
        }

        layoutParams.y = sharedPrefs.getInt("locY", 0);

        windowManager.updateViewLayout(mediaOverlay, layoutParams);
    }

    private void handleVibration() {
        if(sharedPrefs.getBoolean("vibrate", true) && vibrator.hasVibrator()) {
            vibrator.vibrate(50);
        }
    }

    @Override
    public void onPlayPauseClick() {
        handleVibration();

        if(mediaPlayer.getPlaybackStatus() == MediaPlayer.PLAYING) {
            mediaPlayer.sendPause();

            mediaOverlay.setPlayPauseIcon(MediaOverlayView.PLAY);
        }
        else {
            mediaPlayer.sendPlay();

            mediaOverlay.setPlayPauseIcon(MediaOverlayView.PAUSE);
        }
    }

    @Override
    public void onPreviousClick() {
        handleVibration();

        mediaPlayer.sendPrev();
    }

    @Override
    public void onNextClick() {
        handleVibration();

        mediaPlayer.sendNext();
    }

    @Override
    public void onLongClick() {
        handleVibration();

        overlayDropView.setVisibility(View.VISIBLE);

        mediaOverlay.startDrag(null, new View.DragShadowBuilder(mediaOverlay), null, 0);
    }

    private void toastyError(String message, Exception e) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        e.printStackTrace();

        stopSelf();
    }

    private int getLayoutResource() {
        return getResources().getIdentifier(sharedPrefs.getString("skin", "skin_material"), "layout", getPackageName());
    }

    public static class RefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(that != null) {
                that.refresh();
            }
        }

    }

    public static class StopReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(that != null) {
                that.sharedPrefs.edit().putBoolean("started", false).apply();

                that.stopSelf();
            }
        }

    }
}
