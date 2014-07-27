package com.mikekorcha.mediabuttonoverlay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.mikekorcha.mediabuttonoverlay.util.IabHelper;
import com.mikekorcha.mediabuttonoverlay.util.IabResult;
import com.mikekorcha.mediabuttonoverlay.util.Inventory;
import com.mikekorcha.mediabuttonoverlay.util.Purchase;
import com.robobunny.SeekBarPreference;

public class MainActivity extends PreferenceActivity {
    protected MainActivity        parent = this;

    protected ListPreference      player;
    private   ListPreference      location;

    private   SeekBarPreference   opacity;

    private   Preference          start;
    private   Preference          rate;
    private   Preference          donate;
    private   Preference          preventsleep;

    public    SharedPreferences   prefs;

    private   IabHelper           iab;

    private IabHelper.OnIabPurchaseFinishedListener  finished = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            if(result.isFailure()) {
                return;
            }
            else if(info.getSku().equals(parent.getString(R.string.donation_sku))) {
                Toast.makeText(parent, "Thanks for the coffee! :)", Toast.LENGTH_LONG).show();

                parent.donate.setEnabled(false);
                parent.donate.setTitle("Thanks for the coffee!");
                parent.donate.setSummary(":)");
            }
        }
    };

    private IabHelper.QueryInventoryFinishedListener inv = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            if(inv.hasPurchase(parent.getString(R.string.donation_sku))) {
                parent.donate.setEnabled(false);
                parent.donate.setTitle("Thanks for the coffee!");
                parent.donate.setSummary(":)");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);

        this.player    = (ListPreference)    this.findPreference("player");
        this.location  = (ListPreference)    this.findPreference("location");

        this.opacity   = (SeekBarPreference) this.findPreference("opacity");

        this.start     = (Preference)        this.findPreference("start");
        this.rate      = (Preference)        this.findPreference("rate");
        this.donate    = (Preference)        this.findPreference("donate");

        this.preventsleep = (Preference) this.findPreference("preventsleep");

        this.prefs     = PreferenceManager.getDefaultSharedPreferences(this);

        this.iab       = new IabHelper(this, this.getString(R.string.license_key));

        this.iab.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if(!result.isSuccess()) {
                    parent.donate.setEnabled(false);
                }
                else {
                    parent.iab.queryInventoryAsync(parent.inv);
                }
            }
        });

        if(this.player.getValue() == null)
            this.player.setValueIndex(0);

        if(this.location.getValue() == null)
            this.location.setValueIndex(0);

        this.player.setSummary(this.player.getValue().toString());
        this.location.setSummary(this.location.getValue().toString());

        this.player.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(newValue.toString());

                parent.Refresh();

                return true;
            }
        });

        this.location.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(newValue.toString());

                parent.Refresh();

                return true;
            }
        });

        this.opacity.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                parent.Refresh();

                return true;
            }
        });

        this.start.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                parent.startService(new Intent(parent.getApplicationContext(), OverlayService.class));

                return false;
            }
        });

        this.rate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + parent.getPackageName()));
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                    parent.startActivity(i);
                }
                catch (Exception e) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + parent.getPackageName() + "&hl=en"));

                    parent.startActivity(i);
                }

                return false;
            }
        });

        this.donate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                parent.iab.launchPurchaseFlow(parent, parent.getString(R.string.donation_sku), 24, parent.finished);

                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!this.iab.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(this.iab != null)  {
            this.iab.dispose();
        }
    }

    private void Refresh() {
        if(this.prefs.getBoolean("started", false)) {
            Intent i = new Intent();
            i.setAction("com.mikekorcha.mediabuttonoverlay.REFRESH");
            this.sendOrderedBroadcast(i, null);
        }
    }
}
