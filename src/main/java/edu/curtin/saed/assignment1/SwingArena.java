package edu.curtin.saed.assignment1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;
import java.util.List; // So that 'List' means java.util.List and not java.awt.List.
import java.net.URL ;

/**
 * A Swing GUI element that displays a grid on which you can draw images, text and lines.
 */
public class SwingArena extends JPanel implements ActionListener
{
    // Represents the image to draw. You can modify this to introduce multiple images.
    private static final int REMOVE = -1;
    private static final String IMAGE_FILE = "1554047213.png";
    private static final String CITADEL_FILE = "rg1024-isometric-tower.png";
    private static final String UNDAMAGED_WALL = "181478.png";
    private static final String WEAKENED_WALL = "181479.png";
    private final static int NO_WALL = 0;
    private final int WALL_UNDAMAGED = 1;
    private final int WALL_WEAKENED = 2;
    private ImageIcon robot1;
    private ImageIcon citadel;
    private ImageIcon wall_undamaged;
    private ImageIcon wall_weakened;
    private final double CITADEL_X = 4;
    private final double CITADEL_Y = 4;
    private int gridWidth = 9;
    private int gridHeight = 9;
    private double robotX;
    private double robotY;
    private double animationRobotX;
    private double animationRobotY;
    private double gridSquareSize; // Auto-calculated
    private List<ArenaListener> listeners = null;
    private Map<String, XandYObject> robotsMap = new HashMap<>();
    private Map<String, XandYObject> wallMap = new HashMap<>();
    private int[][] wallArray = new int[9][9];
    private int[][] robotArray = new int[9][9];;
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private int animationDuration = 400;
    private int animationInterval = 40;
    private long startTime; // Start time of the animation
    private String animationRobot = "";
    private Timer timer;
    private Object mutex = new Object();
    private int newWeakenedWallX;
    private int newWeakenedWallY;
    private boolean lastCall;
    private int wallState;

    /**
     * Creates a new arena object, loading the robot image.
     */
    public SwingArena()
    {
        // Here's how (in Swing) you get an Image object from an image file that's part of the
        // project's "resources". If you need multiple different images, you can modify this code
        // accordingly.

        // (NOTE: _DO NOT_ use ordinary file-reading operations here, and in particular do not try
        // to specify the file's path/location. That will ruin things if you try to create a
        // distributable version of your code with './gradlew build'. The approach below is how a
        // project is supposed to read its own internal resources, and should work both for
        // './gradlew run' and './gradlew build'.)

        URL url = getClass().getClassLoader().getResource(IMAGE_FILE);
        if(url == null)
        {
            throw new AssertionError("Cannot find image file " + IMAGE_FILE);
        }
        robot1 = new ImageIcon(url);

        url = getClass().getClassLoader().getResource(CITADEL_FILE);
        if(url == null)
        {
            throw new AssertionError("Cannot find image file " + CITADEL_FILE);
        }
        citadel = new ImageIcon(url);

        url = getClass().getClassLoader().getResource(UNDAMAGED_WALL);
        if(url == null)
        {
            throw new AssertionError("Cannot find image file " + UNDAMAGED_WALL);
        }
        wall_undamaged = new ImageIcon(url);

        url = getClass().getClassLoader().getResource(WEAKENED_WALL);
        if(url == null)
        {
            throw new AssertionError("Cannot find image file " + WEAKENED_WALL);
        }
        wall_weakened = new ImageIcon(url);
    }

    /**
     * Moves a robot image to a new grid position. This is highly rudimentary, as you will need
     * many different robots in practice. This method currently just serves as a demonstration.
     */

    public void setRobotPosition(String robotName, XandYObject xandYObject)
    {
        synchronized (mutex){
            robotsMap.put(robotName, xandYObject);

            this.animationRobot = robotName;
            endX = xandYObject.getNewX();
            endY = xandYObject.getNewY();
            startX = xandYObject.getOldX();
            startY = xandYObject.getOldY();

            /************/

            robotArray[endX][endY] = 1;
            robotArray[startX][startY] = 1;

            /************/
            timer = new Timer(animationInterval, this);
            timer.setInitialDelay(0);
            startAnimation();
        }


    }
    public void setRobotPosition(String robotName, XandYObject xandYObject, int FLAG) // Robots' startup positions
    {
        synchronized (mutex){
            robotsMap.put(robotName, xandYObject);
            robotArray[xandYObject.getNewX()][xandYObject.getNewY()] = 1;
            repaint();
        }


    }
    public void setWallPosition(int x, int y, int wallState){
        this.wallState = wallState;
        if(wallState == WALL_WEAKENED){
            newWeakenedWallX = x;
            newWeakenedWallY = y;
        }
        else if(wallState == NO_WALL){
            newWeakenedWallX = x;
            newWeakenedWallY = y;
        }
        else
            wallArray[x][y] = wallState;
    }

    public void startAnimation() {
        startTime = System.currentTimeMillis();
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        synchronized (mutex){
            long elapsedTime = System.currentTimeMillis() - startTime;
            if(timer.isRunning()){
                if (elapsedTime >= animationDuration) {

                    XandYObject xandYObject = robotsMap.get(animationRobot);

                    if(xandYObject.getDestroyed()){
                        robotsMap.remove(animationRobot);
                        System.out.println("Destoyed "+animationRobot);
                        animationRobot = REMOVE+"";
                        animationRobotX = REMOVE;
                        animationRobotY = REMOVE;
                        lastCall = true;
                        wallArray[newWeakenedWallX][newWeakenedWallY] = wallState;
                        repaint();
                    }
                    else{
                        xandYObject = robotsMap.get(animationRobot);
                        xandYObject.setOldX(REMOVE);
                        xandYObject.setOldY(REMOVE);
                        robotsMap.put(animationRobot, xandYObject);
//                robotArray[startX][startY] = 0;
                    }
                    timer.stop();

                } else {

                    double progress = (double) elapsedTime / animationDuration;
                    animationRobotX = (startX + progress * (endX - startX));
                    animationRobotY = (startY + progress * (endY - startY));

                    repaint();
                }
            }

        }

    }


    /**
     * Adds a callback for when the user clicks on a grid square within the arena. The callback
     * (of type ArenaListener) receives the grid (x,y) coordinates as parameters to the
     * 'squareClicked()' method.
     */
    public void addListener(ArenaListener newListener)
    {
        if(listeners == null)
        {
            listeners = new LinkedList<>();
            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent event)
                {
                    int gridX = (int)((double)event.getX() / gridSquareSize);
                    int gridY = (int)((double)event.getY() / gridSquareSize);

                    if(gridX < gridWidth && gridY < gridHeight)
                    {
                        for(ArenaListener listener : listeners)
                        {
                            listener.squareClicked(gridX, gridY);
                        }
                    }
                }
            });
        }
        listeners.add(newListener);
    }



    /**
     * This method is called in order to redraw the screen, either because the user is manipulating
     * the window, OR because you've called 'repaint()'.
     *
     * You will need to modify the last part of this method; specifically the sequence of calls to
     * the other 'draw...()' methods. You shouldn't need to modify anything else about it.
     */
    @Override
    public void paintComponent(Graphics g)
    {
        String robotName = "";

        super.paintComponent(g);
        Graphics2D gfx = (Graphics2D) g;
        gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // First, calculate how big each grid cell should be, in pixels. (We do need to do this
        // every time we repaint the arena, because the size can change.)
        gridSquareSize = Math.min(
                (double) getWidth() / (double) gridWidth,
                (double) getHeight() / (double) gridHeight);

        int arenaPixelWidth = (int) ((double) gridWidth * gridSquareSize);
        int arenaPixelHeight = (int) ((double) gridHeight * gridSquareSize);


        // Draw the arena grid lines. This may help for debugging purposes, and just generally
        // to see what's going on.
        gfx.setColor(Color.GRAY);
        gfx.drawRect(0, 0, arenaPixelWidth - 1, arenaPixelHeight - 1); // Outer edge

        for(int gridX = 1; gridX < gridWidth; gridX++) // Internal vertical grid lines
        {
            int x = (int) ((double) gridX * gridSquareSize);
            gfx.drawLine(x, 0, x, arenaPixelHeight);
        }

        for(int gridY = 1; gridY < gridHeight; gridY++) // Internal horizontal grid lines
        {
            int y = (int) ((double) gridY * gridSquareSize);
            gfx.drawLine(0, y, arenaPixelWidth, y);
        }


        // Invoke helper methods to draw things at the current location.
        // ** You will need to adapt this to the requirements of your application. **

        synchronized (mutex){
            for(Map.Entry<String, XandYObject> b : robotsMap.entrySet()){
                robotX = b.getValue().getNewX();
                robotY = b.getValue().getNewY();
                robotName = b.getKey();

                if(!robotName.equals(animationRobot)){
                    drawImage(gfx, robot1, robotX, robotY);
                    drawLabel(gfx, robotName, robotX, robotY);
                }

            }

            if(!animationRobot.equals("")){
                drawImage(gfx, robot1, animationRobotX, animationRobotY);
                drawLabel(gfx, animationRobot, animationRobotX, animationRobotY);
            }

            int damage;
            for(int i = 0; i < 9; i++){
                for(int j = 0; j < 9; j++){
                    damage = wallArray[i][j];
                    if(damage == WALL_UNDAMAGED)
                        drawImage(gfx, wall_undamaged, i, j);
                    if(damage == WALL_WEAKENED){
                        if(newWeakenedWallX == i && newWeakenedWallY == j){ // checks whether this is the newly damaged wall
                            if(lastCall){
                                drawImage(gfx, wall_weakened, i, j);
                            }
                        }
                        else{
                            drawImage(gfx, wall_weakened, i, j);
                        }
                    }

                }
            }

            drawImage(gfx, citadel, CITADEL_X, CITADEL_Y);
            drawLabel(gfx, "Citadel", CITADEL_X, CITADEL_Y);

        }

    }


    /**
     * Draw an image in a specific grid location. *Only* call this from within paintComponent().
     *
     * Note that the grid location can be fractional, so that (for instance), you can draw an image
     * at location (3.5,4), and it will appear on the boundary between grid cells (3,4) and (4,4).
     *
     * You shouldn't need to modify this method.
     */
    private void drawImage(Graphics2D gfx, ImageIcon icon, double gridX, double gridY)
    {
        // Get the pixel coordinates representing the centre of where the image is to be drawn.
        double x = (gridX + 0.5) * gridSquareSize;
        double y = (gridY + 0.5) * gridSquareSize;

        // We also need to know how "big" to make the image. The image file has a natural width
        // and height, but that's not necessarily the size we want to draw it on the screen. We
        // do, however, want to preserve its aspect ratio.
        double fullSizePixelWidth = (double) robot1.getIconWidth();
        double fullSizePixelHeight = (double) robot1.getIconHeight();

        double displayedPixelWidth, displayedPixelHeight;
        if(fullSizePixelWidth > fullSizePixelHeight)
        {
            // Here, the image is wider than it is high, so we'll display it such that it's as
            // wide as a full grid cell, and the height will be set to preserve the aspect
            // ratio.
            displayedPixelWidth = gridSquareSize;
            displayedPixelHeight = gridSquareSize * fullSizePixelHeight / fullSizePixelWidth;
        }
        else
        {
            // Otherwise, it's the other way around -- full height, and width is set to
            // preserve the aspect ratio.
            displayedPixelHeight = gridSquareSize;
            displayedPixelWidth = gridSquareSize * fullSizePixelWidth / fullSizePixelHeight;
        }

        // Actually put the image on the screen.
        gfx.drawImage(icon.getImage(),
                (int) (x - displayedPixelWidth / 2.0),  // Top-left pixel coordinates.
                (int) (y - displayedPixelHeight / 2.0),
                (int) displayedPixelWidth,              // Size of displayed image.
                (int) displayedPixelHeight,
                null);
    }


    /**
     * Displays a string of text underneath a specific grid location. *Only* call this from within
     * paintComponent().
     *
     * You shouldn't need to modify this method.
     */
    private void drawLabel(Graphics2D gfx, String label, double gridX, double gridY)
    {
        gfx.setColor(Color.BLUE);
        FontMetrics fm = gfx.getFontMetrics();
        gfx.drawString(label,
                (int) ((gridX + 0.5) * gridSquareSize - (double) fm.stringWidth(label) / 2.0),
                (int) ((gridY + 1.0) * gridSquareSize) + fm.getHeight());
    }

    /**
     * Draws a (slightly clipped) line between two grid coordinates.
     *
     * You shouldn't need to modify this method.
     */
    private void drawLine(Graphics2D gfx, double gridX1, double gridY1,
                          double gridX2, double gridY2)
    {
        gfx.setColor(Color.RED);

        // Recalculate the starting coordinate to be one unit closer to the destination, so that it
        // doesn't overlap with any image appearing in the starting grid cell.
        final double radius = 0.5;
        double angle = Math.atan2(gridY2 - gridY1, gridX2 - gridX1);
        double clippedGridX1 = gridX1 + Math.cos(angle) * radius;
        double clippedGridY1 = gridY1 + Math.sin(angle) * radius;

        gfx.drawLine((int) ((clippedGridX1 + 0.5) * gridSquareSize),
                (int) ((clippedGridY1 + 0.5) * gridSquareSize),
                (int) ((gridX2 + 0.5) * gridSquareSize),
                (int) ((gridY2 + 0.5) * gridSquareSize));
    }
    public Map<String, XandYObject> getRobotsMap() {
        return robotsMap;
    }
    public double getCITADEL_X() {
        return CITADEL_X;
    }
    public double getCITADEL_Y() {
        return CITADEL_Y;
    }
    public int[][] getWallArray() {
        return wallArray;
    }
    public int[][] getRobotArray() {
        return robotArray;
    }
}
