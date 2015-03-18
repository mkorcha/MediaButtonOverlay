package com.mikekorcha.mediabuttonoverlay.tasker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.mikekorcha.mediabuttonoverlay.MainActivity;

import com.twofortyfouram.locale.api.R;
import com.twofortyfouram.locale.plugin.bundle.PluginBundleManager;

public class TaskerActivity extends MainActivity {

    private boolean mCancelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.twofortyfouram_locale_help_save_dontsave, menu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            try {
                getSupportActionBar().setIcon(getPackageManager().getApplicationIcon(getCallingPackage()));
            }
            catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mCancelled = item.getItemId() == R.id.twofortyfouram_locale_menu_dontsave;

        finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        if(mCancelled) {
            return;
        }

        String message = sharedPrefs.getString("player", "Default");

        Intent i = new Intent();

        i.putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE, PluginBundleManager.generateBundle(getApplicationContext(), message));
        i.putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BLURB, message);

        setResult(RESULT_OK, i);

        super.finish();
    }

}
