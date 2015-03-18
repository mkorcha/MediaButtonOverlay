package com.mikekorcha.mediabuttonoverlay.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mikekorcha.mediabuttonoverlay.services.OverlayService;
import com.twofortyfouram.locale.plugin.bundle.BundleScrubber;
import com.twofortyfouram.locale.plugin.bundle.PluginBundleManager;

public class TaskerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!com.twofortyfouram.locale.api.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
            return;
        }

        BundleScrubber.scrub(intent);

        Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(bundle);

        if(PluginBundleManager.isBundleValid(bundle)) {
            context.startService(new Intent(context, OverlayService.class));
        }
    }
}

