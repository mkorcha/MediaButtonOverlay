package com.mikekorcha.mediabuttonoverlay.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
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

    private View btnClose, btnApp;

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

        mediaOverlay = new MediaOverlayView(this, getLayoutResource(), layoutParams);
        mediaOverlay.setOnClickListeners(this);

        overlayDropView = new OverlayDropView(this);
        overlayDropView.setVisibility(View.INVISIBLE);

        btnApp = overlayDropView.findViewById(R.id.btnApp);
        btnClose = overlayDropView.findViewById(R.id.btnClose);

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
        // Catching individually because of API changes
        catch (ClassNotFoundException e) {
            toastyError("Media player not properly implemented", e);
        }
        catch(IllegalAccessException e) {
            toastyError("Media player not properly implemented", e);
        }
        catch(NoSuchMethodException e) {
            toastyError("Media player not properly implemented", e);
        }
        catch (InstantiationException e) {
            toastyError("Unable to instantiate controls for media player", e);
        }
        catch(InvocationTargetException e) {
            toastyError("Unable to instantiate controls for media player", e);
        }
    }

    private void refresh() {
        mediaOverlay.setLayout(getLayoutResource());

        mediaOverlay.setColour(sharedPrefs.getInt("colour", getResources().getColor(R.color.primary)));

        mediaOverlay.setOpacity(sharedPrefs.getFloat("opacity", 0.5f));

        mediaOverlay.setOrientation(sharedPrefs.getString("orientation", "0").equals("0") ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);

        layoutParams.x = sharedPrefs.getInt("locX", 0);
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
    public void onDrag(View view, MotionEvent motionEvent, int screenX, int screenY) {
        if(overlayDropView.getVisibility() != View.VISIBLE) {
            overlayDropView.setVisibility(View.VISIBLE);
        }

        layoutParams.x = screenX;
        layoutParams.y = screenY;
        windowManager.updateViewLayout(mediaOverlay, layoutParams);
    }

    @Override
    public void onDrop(View view, MotionEvent motionEvent, int screenX, int screenY) {
        Log.d("MBO", Integer.toString(screenY));
        if (OverlayDropView.isOverButton(btnClose, motionEvent)) {
            handleVibration();

            sendBroadcast(new Intent(getPackageName() + ".STOP"));
        }
        else if (OverlayDropView.isOverButton(btnApp, motionEvent)) {
            handleVibration();

            OverlayDropView.openActivity(this);
        }
        else {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            sharedPrefs.edit()
                    .putInt("locX", screenX)
                    .putInt("locY", screenY).apply();

            sendBroadcast(new Intent(getPackageName() + ".REFRESH"));
        }

        overlayDropView.setVisibility(View.INVISIBLE);
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
