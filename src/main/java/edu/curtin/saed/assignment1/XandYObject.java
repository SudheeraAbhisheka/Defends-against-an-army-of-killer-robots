package edu.curtin.saed.assignment1;

import java.util.concurrent.ExecutorService;

public class XandYObject {
    private int x, y;
    private int delay;
    private ExecutorService executorService;

    public XandYObject(int x, int y, int delay) {
        this.x = x;
        this.y = y;
        this.delay = delay;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDelay() {
        return delay;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "XandYObject{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
