package com.mikekorcha.mediabuttonoverlay.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mikekorcha.mediabuttonoverlay.R;

public class OverlayDropView extends FrameLayout {

    private Context context;

    private SharedPreferences sharedPrefs;

    private ImageView btnClose, dropLeft, dropRight;

    public OverlayDropView(final Context context) {
        super(context);

        this.context = context;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        inflate(context, R.layout.layout_movement, this);

        btnClose = (ImageView) findViewById(R.id.btnClose);
        btnClose.setOnDragListener(new CloseDroppable());

        dropLeft = (ImageView) findViewById(R.id.dropLeft);
        dropRight = (ImageView) findViewById(R.id.dropRight);

        dropLeft.setOnDragListener(new SideDroppable(dropLeft, MediaOverlayView.LEFT));
        dropRight.setOnDragListener(new SideDroppable(dropRight, MediaOverlayView.RIGHT));
    }

    private class CloseDroppable implements OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch(event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                // Shadow has entered the bounding box for the button
                case DragEvent.ACTION_DRAG_ENTERED:
                    btnClose.setColorFilter(getResources().getColor(R.color.md_red_700));
                    return true;

                // Shadow left the bounding box
                case DragEvent.ACTION_DRAG_EXITED:
                    btnClose.setColorFilter(getResources().getColor(R.color.md_black));
                    return true;

                // Shadow gets dropped onto the button
                case DragEvent.ACTION_DROP:
                    context.sendBroadcast(new Intent(context.getPackageName() + ".STOP"));
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    setVisibility(INVISIBLE);
                    return true;
            }

            return false;
        }
    }

    private class SideDroppable implements OnDragListener {

        private View view;
        private int  side;

        public SideDroppable(View view, int side) {
            this.view = view;
            this.side = side;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch(event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    view.setBackgroundColor(view.getContext().getResources().getColor(R.color.md_white));
                    view.setAlpha(.6f);
                    return true;

                // Shadow left the bounding box
                case DragEvent.ACTION_DRAG_EXITED:
                    view.setBackgroundColor(Color.TRANSPARENT);
                    return true;

                // Shadow gets dropped onto the button
                case DragEvent.ACTION_DROP:
                    sharedPrefs.edit().putInt("location", side).commit();

                    // LayoutParams is stupid
                    int height = getContext().getResources().getDisplayMetrics().heightPixels / 2;

                    if(event.getY() > height) {
                        sharedPrefs.edit().putInt("locY", -((int) -event.getY() + height)).commit();
                    }
                    else {
                        sharedPrefs.edit().putInt("locY", -(height - (int) event.getY())).commit();
                    }

                    context.sendBroadcast(new Intent(context.getPackageName() + ".REFRESH"));

                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:

                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    setVisibility(INVISIBLE);
                    return true;
            }

            return false;
        }
    }
}
