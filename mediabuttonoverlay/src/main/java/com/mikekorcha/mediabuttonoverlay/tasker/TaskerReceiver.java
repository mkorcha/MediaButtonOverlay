package com.mikekorcha.mediabuttonoverlay.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.mikekorcha.mediabuttonoverlay.OverlayService;

import net.dinglisch.tasker.bundle.BundleScrubber;
import net.dinglisch.tasker.bundle.PluginBundleManager;

public class TaskerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent){
        if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) return;

        BundleScrubber.scrub(intent);

        Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(intent);

        if(PluginBundleManager.isBundleValid(bundle))
            context.startService(new Intent(context, OverlayService.class));
    }
}