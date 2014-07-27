package com.mikekorcha.mediabuttonoverlay;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

public class MediaControlView extends LinearLayout {

    private LinearLayout layout;

    protected ImageView prev;
    protected ImageView pp;
    protected ImageView next;
    protected ImageView close;

    public MediaControlView(Context context) {
        super(context);

        View view = this.inflate(getContext(), R.layout.media_control_view, null);

        this.layout = (LinearLayout) view.findViewById(R.id.layout);

        this.prev = (ImageView) view.findViewById(R.id.prev);
        this.pp = (ImageView) view.findViewById(R.id.pp);
        this.next = (ImageView) view.findViewById(R.id.next);
        this.close = (ImageView) view.findViewById(R.id.close);

        this.addView(view);
    }

    public void setOpacity(float opacity) {
        this.prev.setAlpha(opacity);
        this.pp.setAlpha(opacity);
        this.next.setAlpha(opacity);
        this.close.setAlpha(opacity);
    }
}