package com.mikekorcha.mediabuttonoverlay;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikekorcha.mediabuttonoverlay.services.OverlayService;

public class MainActivity extends ActionBarActivity {

    private static MainActivity that;

    protected SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        that = this;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        getFragmentManager().beginTransaction().replace(R.id.content, new PrefFragment()).commit();

        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartClick();
            }
        });
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

    void onStartClick() {
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

    }

    public static class StartReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            that.onStartClick();
        }
    }
}
