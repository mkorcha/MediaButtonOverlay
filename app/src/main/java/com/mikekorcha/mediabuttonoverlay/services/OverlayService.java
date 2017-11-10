package com.mikekorcha.mediabuttonoverlay.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mikekorcha.mediabuttonoverlay.MainActivity;
import com.mikekorcha.mediabuttonoverlay.views.MediaOverlayView;
import com.mikekorcha.mediabuttonoverlay.MediaPlayer;
import com.mikekorcha.mediabuttonoverlay.R;
import com.mikekorcha.mediabuttonoverlay.views.OverlayDropView;

import java.lang.reflect.InvocationTargetException;

public class OverlayService extends Service implements MediaOverlayView.OnMediaButtonClickListeners {

    private static OverlayService that;

    private static int NOTIFICATION_ID = 42;

    private WindowManager windowManager;

    private Vibrator vibrator;

    private SharedPreferences sharedPrefs;

    private WindowManager.LayoutParams layoutParams =
            getParams(WindowManager.LayoutParams.WRAP_CONTENT);

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

        windowManager.addView(overlayDropView, getParams(WindowManager.LayoutParams.MATCH_PARENT));
        windowManager.addView(mediaOverlay, layoutParams);

        refresh();

        if(sharedPrefs.getBoolean("startapp", false)) {
            mediaPlayer.launchPlayer();
        }

        // set up notification for the foreground
        if(sharedPrefs.getBoolean("notification", false)) {
            // I don't feel the need to double-up the notification
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, MainActivity.getForegroundNotification(this));
        return START_NOT_STICKY;
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

        if(sharedPrefs.getBoolean("notification", false)) {
            // I don't feel the need to double-up the notification
            MainActivity.startNotification(this);
        }

        super.onDestroy();
        stopForeground(true);
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
        if (OverlayDropView.isOverButton(btnClose, motionEvent)) {
            handleVibration();

            sendBroadcast(new Intent(getBaseContext(), StopReceiver.class));
        }
        else if (OverlayDropView.isOverButton(btnApp, motionEvent)) {
            handleVibration();

            OverlayDropView.openActivity(this);
        }
        else {
            sharedPrefs.edit()
                    .putInt("locX", screenX)
                    .putInt("locY", screenY).apply();

            sendBroadcast(new Intent(getBaseContext(), RefreshReceiver.class));
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

    private static WindowManager.LayoutParams getParams(int sizing) {
        return new WindowManager.LayoutParams(
                sizing, sizing,
                Build.VERSION.SDK_INT > 25 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
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
