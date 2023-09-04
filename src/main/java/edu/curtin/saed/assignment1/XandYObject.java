package edu.curtin.saed.assignment1;

public class XandYObject {
    private double x, y;

    public XandYObject(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "XandYObject{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
