package com.mikekorcha.mediabuttonoverlay.views;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mikekorcha.mediabuttonoverlay.R;

import java.util.ArrayList;

public class MediaOverlayView extends LinearLayout {

    public static final int LEFT  = 0;
    public static final int RIGHT = 1;

    public static final int PLAY  = 0;
    public static final int PAUSE = 1;

    private ImageView btnPrevious, btnNext, btnPlayPause;

    private LinearLayout buttonLayout;

    private OnMediaButtonClickListeners onMediaButtonClickListeners;

    public MediaOverlayView(Context context) {
        super(context);
    }

    public MediaOverlayView(Context context, int layout) {
        super(context);

        setLayout(layout);
    }

    public void setOpacity(float opacity) {
        btnPrevious.setAlpha(opacity);
        btnNext.setAlpha(opacity);
        btnPlayPause.setAlpha(opacity);
    }

    public void setColour(int colour) {
        for(View v : getViewsByTag(this, "recolourable")) {
            if(v instanceof FloatingActionButton) {
                FloatingActionButton floatingActionButton = (FloatingActionButton) v;

                floatingActionButton.setColorNormal(colour);
                floatingActionButton.setColorPressed(darken(colour));
            }
            else {
                v.setBackgroundColor(colour);
            }
        }
    }

    public void setLayout(int layout) {
        removeAllViews();

        inflate(getContext(), layout, this);

        btnPrevious =  (ImageView) findViewById(R.id.btnPrevious);
        btnPlayPause = (ImageView) findViewById(R.id.btnPlayPause);
        btnNext = (ImageView) findViewById(R.id.btnNext);

        buttonLayout = (LinearLayout) findViewById(R.id.buttonLayout);

        if(onMediaButtonClickListeners != null) {
            setOnClickListeners(onMediaButtonClickListeners);
        }
    }

    public void setPlayPauseIcon(int button) {
        int icon = button == PLAY ? R.drawable.ic_play : R.drawable.ic_pause;

        if(btnPlayPause instanceof FloatingActionButton) {
            ((FloatingActionButton) btnPlayPause).setIcon(icon);
        }
        else {
            btnPlayPause.setImageResource(icon);
        }
    }

    @Override
    public void setOrientation(int orientation) {
        buttonLayout.setOrientation(orientation);
    }

    public void setOnClickListeners(OnMediaButtonClickListeners listeners) {
        onMediaButtonClickListeners = listeners;

        btnPrevious.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onMediaButtonClickListeners.onPreviousClick();
            }
        });

        btnPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onMediaButtonClickListeners.onPlayPauseClick();
            }
        });

        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onMediaButtonClickListeners.onNextClick();
            }
        });


        OnLongClickListener onLongClickListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onMediaButtonClickListeners.onLongClick();

                return true;
            }
        };

        buttonLayout.setOnLongClickListener(onLongClickListener);
        btnPrevious.setOnLongClickListener(onLongClickListener);
        btnPlayPause.setOnLongClickListener(onLongClickListener);
        btnNext.setOnLongClickListener(onLongClickListener);
    }

    private static int darken(int colour) {
        return Color.argb(
                Color.alpha(colour),
                Math.max((int) (Color.red(colour)   * .75), 0),
                Math.max((int) (Color.green(colour) * .75), 0),
                Math.max((int) (Color.blue(colour)  * .75), 0)
        );
    }

    // From http://stackoverflow.com/a/16262479/1746398
    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<>();
        final int childCount = root.getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }

        return views;
    }

    public interface OnMediaButtonClickListeners {
        public void onPlayPauseClick();
        public void onPreviousClick();
        public void onNextClick();

        public void onLongClick();
    }
}
