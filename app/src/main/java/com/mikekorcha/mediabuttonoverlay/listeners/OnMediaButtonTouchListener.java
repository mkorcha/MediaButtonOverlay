package com.mikekorcha.mediabuttonoverlay.listeners;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


public abstract class OnMediaButtonTouchListener implements View.OnTouchListener {

    private boolean isMoving;

    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    private WindowManager.LayoutParams layoutParams;

    protected OnMediaButtonTouchListener(WindowManager.LayoutParams params) {
        this.layoutParams = params;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        int[] coords = getCoords(motionEvent);
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = layoutParams.x;
                initialY = layoutParams.y;
                initialTouchX = motionEvent.getRawX();
                initialTouchY = motionEvent.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                if(coords[0] < initialX || coords[0] > initialX + view.getWidth()
                        || coords[1] < initialY || coords[1] > initialY + view.getHeight()) {
                    isMoving = true;

                    onDrag(view, motionEvent, coords[0], coords[1]);
                }
                return true;
    
            case MotionEvent.ACTION_UP:
                if(isMoving) {
                    isMoving = false;

                    onDrop(view, motionEvent, coords[0], coords[1]);
                }
                else {
                    onClick();
                }
                return true;
        }
        return false;
    }

    private int[] getCoords(MotionEvent motionEvent) {
        return new int[] {initialX + (int) (motionEvent.getRawX() - initialTouchX),
                          initialY + (int) (motionEvent.getRawY() - initialTouchY)};
    }

    public abstract void onClick();
    public abstract void onDrag(View view, MotionEvent motionEvent, int screenX, int screenY);
    public abstract void onDrop(View view, MotionEvent motionEvent, int screenX, int screenY);
}
