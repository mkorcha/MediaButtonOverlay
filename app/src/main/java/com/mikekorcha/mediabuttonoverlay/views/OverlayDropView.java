package com.mikekorcha.mediabuttonoverlay.views;

import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mikekorcha.mediabuttonoverlay.MainActivity;
import com.mikekorcha.mediabuttonoverlay.R;

public class OverlayDropView extends FrameLayout {

    public OverlayDropView(final Context context) {
        super(context);

        inflate(context, R.layout.layout_movement, this);

        ImageView btnClose = findViewById(R.id.btnClose);
        ImageView btnApp   = findViewById(R.id.btnApp);

        btnClose.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                context.sendBroadcast(new Intent(context.getPackageName() + ".STOP"));

                return false;
            }
        });

        btnApp.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openActivity(context);

                return false;
            }
        });

        OnClickListener buttonListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(INVISIBLE);
            }
        };

        btnClose.setOnClickListener(buttonListener);
        btnApp.setOnClickListener(buttonListener);
    }

    public static void openActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    public static boolean isOverButton(View view, MotionEvent event) {
        return event.getRawX() >= view.getLeft() && event.getRawX() <= view.getRight() &&
                (event.getRawY() - getStatusBarHeight(view.getContext())) >= view.getTop()  && (event.getRawY() - getStatusBarHeight(view.getContext())) <= view.getBottom();
    }

    // TODO find a nicer way to do this that supports fullscreen apps as well
    // note may not be needed after setting wm to true fullscreen
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
