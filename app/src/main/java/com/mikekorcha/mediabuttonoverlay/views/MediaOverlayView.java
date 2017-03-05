package com.mikekorcha.mediabuttonoverlay.views;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mikekorcha.mediabuttonoverlay.R;
import com.mikekorcha.mediabuttonoverlay.listeners.OnMediaButtonTouchListener;

import java.util.ArrayList;

public class MediaOverlayView extends LinearLayout {

    public static final int LEFT  = 0;
    public static final int RIGHT = 1;

    public static final int PLAY  = 0;
    public static final int PAUSE = 1;

    private ImageView btnPrevious, btnNext, btnPlayPause;

    private LinearLayout buttonLayout;

    private OnMediaButtonClickListeners onMediaButtonClickListeners;

    private WindowManager.LayoutParams layoutParams;

    public MediaOverlayView(Context context) {
        super(context);
    }

    public MediaOverlayView(Context context, int layout, WindowManager.LayoutParams params) {
        super(context);

        setLayout(layout);

        this.layoutParams = params;
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

        btnPrevious.setOnTouchListener(new OnMediaButtonTouchListener(layoutParams) {
            @Override
            public void onClick() {
                onMediaButtonClickListeners.onPreviousClick();
            }

            @Override
            public void onDrag(View view, MotionEvent motionEvent, int screenX, int screenY) {
                onMediaButtonClickListeners.onDrag(view, motionEvent, screenX, screenY);
            }

            @Override
            public void onDrop(View view, MotionEvent motionEvent, int screenX, int screenY) {
                onMediaButtonClickListeners.onDrop(view, motionEvent, screenX, screenY);
            }
        });

        btnPlayPause.setOnTouchListener(new OnMediaButtonTouchListener(layoutParams) {
            @Override
            public void onClick() {
                onMediaButtonClickListeners.onPlayPauseClick();
            }

            @Override
            public void onDrag(View view, MotionEvent motionEvent, int screenX, int screenY) {
                onMediaButtonClickListeners.onDrag(view, motionEvent, screenX, screenY);
            }

            @Override
            public void onDrop(View view, MotionEvent motionEvent, int screenX, int screenY) {
                onMediaButtonClickListeners.onDrop(view, motionEvent, screenX, screenY);
            }
        });

        btnNext.setOnTouchListener(new OnMediaButtonTouchListener(layoutParams) {
            @Override
            public void onClick() {
                onMediaButtonClickListeners.onNextClick();
            }

            @Override
            public void onDrag(View view, MotionEvent motionEvent, int screenX, int screenY) {
                onMediaButtonClickListeners.onDrag(view, motionEvent, screenX, screenY);
            }

            @Override
            public void onDrop(View view, MotionEvent motionEvent, int screenX, int screenY) {
                onMediaButtonClickListeners.onDrop(view, motionEvent, screenX, screenY);
            }
        });
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
        void onPlayPauseClick();
        void onPreviousClick();
        void onNextClick();

        void onDrag(View view, MotionEvent motionEvent, int screenX, int screenY);
        void onDrop(View view, MotionEvent motionEvent, int screenX, int screenY);
    }
}
