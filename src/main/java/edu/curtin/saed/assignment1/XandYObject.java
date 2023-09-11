package edu.curtin.saed.assignment1;

import java.util.Timer;

public class XandYObject {
    private int newX, newY, oldX, oldY;
    private final int delay;
    private boolean destroyed = false;
    private Timer timer = new java.util.Timer();
    private long startTime;
    private boolean timerStart = false;
    private final int ANIMATION_DURATION = 400;

    public XandYObject(int newX, int newY, int delay) {
        this.newX = newX;
        this.newY = newY;
        this.delay = delay;
    }
    public void startTimer(){
        startTime = System.currentTimeMillis();
        timerStart = true;
    }

    public double[] getAnimatedCoordinates(){
        long elapsedTime = System.currentTimeMillis() - startTime;

        int endX = getNewX();
        int endY = getNewY();
        int startX = getOldX();
        int startY = getOldY();

        double animatedX;
        double animatedY;

        double[] animatedCoordinates = new double[2];

        if (elapsedTime >= ANIMATION_DURATION) {
            timerStart = false;
        } else {

            double progress = (double) elapsedTime / ANIMATION_DURATION;
            animatedX = (startX + progress * (endX - startX));
            animatedY = (startY + progress * (endY - startY));

            animatedCoordinates[0] = animatedX;
            animatedCoordinates[1] = animatedY;
        }

        return animatedCoordinates;
    }

    public boolean isTimerStart(){
        return timerStart;
    }
    public int getOldX() {
        return oldX;
    }
    public int getOldY() {
        return oldY;
    }
    public int getNewX() {
        return newX;
    }
    public int getNewY() {
        return newY;
    }

    public int getDelay() {
        return delay;
    }
    public Timer getTimer() {
        return timer;
    }

    public void setNewX(int newX) {
        this.newX = newX;
    }

    public void setNewY(int newY) {
        this.newY = newY;
    }
    public void setOldX(int oldX) {
        this.oldX = oldX;
    }
    public void setOldY(int oldY) {
        this.oldY = oldY;
    }
    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    @Override
    public String toString() {
        return "XandYObject{" +
                "x=" + newX +
                ", y=" + newY +
                '}';
    }
}
