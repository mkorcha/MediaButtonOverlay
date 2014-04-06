package com.mikekorcha.mediabuttonoverlay.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mikekorcha.mediabuttonoverlay.MainActivity;
import com.mikekorcha.mediabuttonoverlay.OverlayService;
import com.mikekorcha.mediabuttonoverlay.R;

import net.dinglisch.tasker.Constants;
import net.dinglisch.tasker.bundle.BundleScrubber;
import net.dinglisch.tasker.bundle.PluginBundleManager;

public class TaskerActivity extends MainActivity {

    boolean mIsCancelled = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void finish() {
        if(this.mIsCancelled) return;

        String message = this.player.toString();

        Intent resultI = new Intent();
        Bundle resultB = PluginBundleManager.generateBundle(getApplicationContext(), message);

        resultI.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultB);

        resultI.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, message);

        setResult(RESULT_OK, resultI);

        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(com.twofortyfouram.locale.api.R.menu.twofortyfouram_locale_help_save_dontsave, menu);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            getActionBar().setIcon(getPackageManager().getApplicationIcon(getCallingPackage()));
        }

        catch (final PackageManager.NameNotFoundException e) {
            if (Constants.IS_LOGGABLE)
                Log.w(Constants.LOG_TAG, "An error occurred loading the host's icon", e); //$NON-NLS-1$
        }

        return true;
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item)
    {
        final int id = item.getItemId();

        if (android.R.id.home == id) {
            this.finish();
            return true;
        }

        else if (R.id.twofortyfouram_locale_menu_dontsave == id) {
            mIsCancelled = true;
            this.finish();
            return true;
        }

        else if (R.id.twofortyfouram_locale_menu_save == id) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
