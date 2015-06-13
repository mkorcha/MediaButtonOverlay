package com.mikekorcha.mediabuttonoverlay;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikekorcha.mediabuttonoverlay.services.OverlayService;
import com.mikekorcha.mediabuttonoverlay.views.MediaOverlayView;

public class MainActivity extends ActionBarActivity {

    private static MainActivity that;

    protected SharedPreferences sharedPrefs;

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

        that = this;

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
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

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

    public void startOverlay() {
        if(!sharedPrefs.getBoolean("started", false)) {
            startService(new Intent(getApplicationContext(), OverlayService.class));

            sharedPrefs.edit().putBoolean("started", true).apply();
        }
        else {
            sendBroadcast(new Intent(getPackageName() + ".STOP"));

            sharedPrefs.edit().putBoolean("started", false).apply();
        }
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
                getActivity().sendBroadcast(new Intent(getActivity().getPackageName() + ".REFRESH"));
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if(preference.getKey().equals("start")) {
                that.startOverlay();
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    public static class StartReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            that.startOverlay();
        }
    }
}
