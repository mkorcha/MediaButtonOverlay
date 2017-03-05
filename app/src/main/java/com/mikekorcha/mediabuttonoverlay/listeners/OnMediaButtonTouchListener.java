package com.mikekorcha.mediabuttonoverlay.listeners;

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

    public OnMediaButtonTouchListener(WindowManager.LayoutParams params) {
        this.layoutParams = params;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = layoutParams.x;
                initialY = layoutParams.y;
                initialTouchX = motionEvent.getRawX();
                initialTouchY = motionEvent.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                if(Math.abs(motionEvent.getX()) < view.getWidth() ||
                        Math.abs(motionEvent.getY()) < view.getHeight()) {
                    isMoving = true;

                    int[] coords = getCoords(motionEvent);
                    onDrag(view, motionEvent, coords[0], coords[1]);
                }
                return true;

            case MotionEvent.ACTION_UP:
                if(isMoving) {
                    isMoving = false;

                    int[] coords = getCoords(motionEvent);
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
