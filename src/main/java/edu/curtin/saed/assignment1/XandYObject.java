package edu.curtin.saed.assignment1;

import java.util.concurrent.ExecutorService;

public class XandYObject {
    private int newX, newY, oldX, oldY;
    private int delay;
    private ExecutorService executorService;

    public XandYObject(int newX, int newY, int delay) {
        this.newX = newX;
        this.newY = newY;
        this.delay = delay;
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


    @Override
    public String toString() {
        return "XandYObject{" +
                "x=" + newX +
                ", y=" + newY +
                '}';
    }
}
