package com.mikekorcha.mediabuttonoverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mikekorcha.mediabuttonoverlay.services.OverlayService;
import com.mikekorcha.mediabuttonoverlay.views.MediaOverlayView;

public class MainActivity extends AppCompatActivity {

    protected SharedPreferences sharedPrefs;

    private static int REQUEST_CODE = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Fix settings from previous version to work with this version, if needed
        String player = sharedPrefs.getString("player", "Default");

        if(player.equals("Google Play")) {
            sharedPrefs.edit().putString("player", "PlayMusic").apply();
        }
        else if(player.equals("Default Music Player")) {
            sharedPrefs.edit().putString("player", "Default").apply();
        }

        try {
            // Will fail if it's actually a string, indicating old version, which drops to the catch
            // which will fix it
            sharedPrefs.getInt("location", 0);
        }
        catch(ClassCastException e) {
            if(sharedPrefs.getString("location", "Left").equals("Left")) {
                sharedPrefs.edit().putInt("location", MediaOverlayView.LEFT).apply();
            }
            else {
                sharedPrefs.edit().putInt("location", MediaOverlayView.RIGHT).apply();
            }
        }

        try {
            sharedPrefs.getFloat("opacity", 0.5f);
        }
        catch(ClassCastException e) {
            sharedPrefs.edit().putFloat("opacity", sharedPrefs.getInt("opacity", 0) / 100).apply();
        }

        setContentView(R.layout.activity_main);

        if(sharedPrefs.getBoolean("notification", false)) {
            startNotification(getApplicationContext());
        }

        if(Build.VERSION.SDK_INT >= 26) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(getPackageName(), "Media Button Overlay", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Persisting notifications for Media Button Overlay");
            channel.enableLights(false);
            channel.enableVibration(false);
            manager.createNotificationChannel(channel);
        }

        getFragmentManager().beginTransaction().replace(R.id.content, new PrefFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_rate) {
            int intent_flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;

            if(Build.VERSION.SDK_INT < 21) {
                //noinspection deprecation
                intent_flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
            }
            else {
                intent_flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
            }

            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
            i.addFlags(intent_flags);

            startActivity(i);

            return true;
        }

        else if(id == R.id.action_github) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mkorcha/MediaButtonOverlay")));

            return true;
        }

        else if(id == R.id.action_about) {
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.action_about))
                    .setMessage(getResources().getString(R.string.about))
                    .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE) {
            startService(this);

            sharedPrefs.edit().putBoolean("started", true).apply();
        }
        else {
            Toast.makeText(this, "To use the overlay, please allow the app to draw on the screen.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void toggleOverlay(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if(!sharedPrefs.getBoolean("started", false)) {
            if(Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(context)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                ((Activity) context).startActivityForResult(i, REQUEST_CODE);
            }
            else {
                startService(context);

                sharedPrefs.edit().putBoolean("started", true).apply();
            }
        }
        else {
            context.sendBroadcast(new Intent(context, OverlayService.StopReceiver.class));

            sharedPrefs.edit().putBoolean("started", false).apply();
        }
    }

    public static void startService(Context context) {
        Intent service = new Intent(context, OverlayService.class);
        if(Build.VERSION.SDK_INT > 25) {
            context.startForegroundService(service);
        }
        else {
            context.startService(service);
        }
    }

    public static void startNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i = new Intent(context, StartReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = getNotification(context, pendingIntent, R.string.notification_info);
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(1, notification);
    }

    public static Notification getForegroundNotification(Context context) {
        Intent notificationIntent = new Intent(context, OverlayService.StopReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return getNotification(context, pendingIntent, R.string.notification_running);
    }

    public static Notification getNotification(Context context, PendingIntent pendingIntent, int text_resource) {
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(context.getResources().getString(text_resource))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true);
        if(Build.VERSION.SDK_INT < 26) {
            builder.setPriority(Notification.PRIORITY_LOW);
        }
        else {
            builder.setChannelId(context.getPackageName());
        }
        if(Build.VERSION.SDK_INT < 16) {
            return builder.getNotification();
        }
        return builder.build();
    }

    // Now that addPreferencesFromResource is deprecated I have to do this, which does the same
    // thing with many more lines of code! x.x
    public static class PrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.prefs);

            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(getActivity() != null) {
                getActivity().sendBroadcast(new Intent(getActivity(), OverlayService.RefreshReceiver.class));
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if(preference.getKey().equals("start")) {
                toggleOverlay(getActivity());
            }
            else if(preference.getKey().equals("notification")) {
                if(preferenceScreen.getSharedPreferences().getBoolean(preference.getKey(), false)) {
                    startNotification(getActivity());
                }
                else {
                    ((NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
                }
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    public static class StartReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            toggleOverlay(context);
        }
    }

    public static class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notification", false)) {
                startNotification(context);
            }
        }
    }
}
