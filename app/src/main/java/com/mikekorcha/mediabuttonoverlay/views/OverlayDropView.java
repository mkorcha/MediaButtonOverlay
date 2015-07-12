package com.mikekorcha.mediabuttonoverlay.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mikekorcha.mediabuttonoverlay.MainActivity;
import com.mikekorcha.mediabuttonoverlay.R;

public class OverlayDropView extends FrameLayout {

    private SharedPreferences sharedPrefs;

    private ImageView btnClose;
    private ImageView btnApp;

    public OverlayDropView(final Context context) {
        super(context);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        inflate(context, R.layout.layout_movement, this);

        btnClose = (ImageView) findViewById(R.id.btnClose);
        btnApp   = (ImageView) findViewById(R.id.btnApp);

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
                openActivity();

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

        setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    // Check if the overlay is dragged over the circle, and stop it if so
                    if (isOverButton(btnClose, event)) {
                        context.sendBroadcast(new Intent(context.getPackageName() + ".STOP"));
                    }
                    // Check if overlay is dragged over the icon to open the app, and open it if so
                    else if (isOverButton(btnApp, event)) {
                        openActivity();
                    }
                    else {
                        sharedPrefs.edit()
                                .putInt("location", getSide(event.getX()))
                                .putInt("locY", calcY(event.getY())).commit();

                        context.sendBroadcast(new Intent(context.getPackageName() + ".REFRESH"));
                    }

                    setVisibility(INVISIBLE);
                }

                return true;
            }

        });
    }

    private void openActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        getContext().startActivity(intent);
    }

    private boolean isOverButton(View view, DragEvent event) {
        return event.getX() >= view.getLeft() && event.getX() <= view.getRight() &&
                event.getY() >= view.getTop()  && event.getY() <= view.getBottom();
    }

    // Calculates a Y-coordinate on-screen to put the overlay
    private int calcY(float eventZ) {
        int height = getContext().getResources().getDisplayMetrics().heightPixels / 2;

        // Why does it have to be this way? ;-;
        if(eventZ > height) {
            return -((int) -eventZ + height);
        }

        return -(height - (int) eventZ);
    }

    // Determines which side of the screen the overlay was dropped on
    private int getSide(float eventX) {
        int width = getContext().getResources().getDisplayMetrics().widthPixels / 2;

        return eventX > width ? MediaOverlayView.RIGHT : MediaOverlayView.LEFT;
    }
}
