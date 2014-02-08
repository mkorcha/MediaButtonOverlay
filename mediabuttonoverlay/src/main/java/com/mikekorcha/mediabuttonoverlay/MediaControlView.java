package com.mikekorcha.mediabuttonoverlay;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MediaControlView extends LinearLayout {

    private LinearLayout layout;

    private ImageView    prev;
    private ImageView    pp;
    private ImageView    next;
    private ImageView    close;

    public MediaControlView(Context context) {
        super(context);

        View view = this.inflate(getContext(), R.layout.media_control_view, null);

        this.layout = (LinearLayout) view.findViewById(R.id.layout);

        this.prev  = (ImageView) view.findViewById(R.id.prev);
        this.pp    = (ImageView) view.findViewById(R.id.pp);
        this.next  = (ImageView) view.findViewById(R.id.next);
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
