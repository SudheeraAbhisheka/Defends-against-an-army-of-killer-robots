package edu.curtin.saed.assignment1;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;
import javax.swing.*;

public class App
{
    private static JTextArea logger;
    private static ExecutorService executorService;
    private static SwingArena arena;
    private static int robotNumber = 1;
    private static final int OCCUPIED_ROBOTS_WALLS = 3;
    private static final int OCCUPIED_WALLS = 2;
    private final static int OCCUPIED_ALL = 4;
    private final static int OCCUPIED_WEAKENED_WALLS = 6;
    private final static int OCCUPIED_UNDAMAGED_WALLS = 7;
    private final static int OCCUPIED_ROBOTS = 5;
    private final static int WALL_UNDAMAGED = 1;
    private final static int WALL_WEAKENED = 2;
    private final static int NO_WALL = 0;
    private static int wallCount;
    private final static int ROBOTS_LIMIT = 16; // temporary

    public static void main(String[] args)
    {
        BlockingQueue<XandYObject> blockingQueue = new ArrayBlockingQueue<>(20);
        final int OCCUPIED_CITADEL = 1;
        final int NO_DELAY = 0;

        executorService = Executors.newCachedThreadPool();

        // Note: SwingUtilities.invokeLater() is equivalent to JavaFX's Platform.runLater().
        SwingUtilities.invokeLater(() ->
        {
            JFrame window = new JFrame("Example App (Swing)");
            arena = new SwingArena();

            CompletableFuture.runAsync(App::RobotsAppearing, executorService);
            CompletableFuture.runAsync(() -> arena.addListener((x, y) ->
            {
                String s;

                if(!IsOccupied(x, y, OCCUPIED_CITADEL)){
                    s = String.format("%s, %s", x, y);
                    logger.append(s+"\n");

                    blockingQueue.add(new XandYObject(x, y, NO_DELAY));
                }
            }));

//            arena.addListener((x, y) ->
//            {
////                System.out.println("Arena click at (" + x + "," + y + ")");
//                String s = String.format("%s, %s", x, y);
//                logger.append(s+"\n");
//
//                if(!IsOccupied(x, y, OCCUPIED_CITADEL)){
//                    if(wallsCount < 10){
//                        blockingQueue.add(new XandYObject(x, y, NO_DELAY));
//                        wallsCount++;
//                    }
//                }
//            });

            CompletableFuture.runAsync(() -> FortressWall(blockingQueue));


            JToolBar toolbar = new JToolBar();
//             JButton btn1 = new JButton("My Button 1");
//             JButton btn2 = new JButton("My Button 2");
            JLabel label = new JLabel("Score: 999");
//             toolbar.add(btn1);
//             toolbar.add(btn2);
            toolbar.add(label);

//             btn1.addActionListener((event) ->
//             {
//                 System.out.println("Button 1 pressed");
//             });

            logger = new JTextArea();
            JScrollPane loggerArea = new JScrollPane(logger);
            loggerArea.setBorder(BorderFactory.createEtchedBorder());

            JSplitPane splitPane = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT, arena, logger);

            Container contentPane = window.getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(toolbar, BorderLayout.NORTH);
            contentPane.add(splitPane, BorderLayout.CENTER);

            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setPreferredSize(new Dimension(800, 800));
            window.pack();
            window.setVisible(true);

            splitPane.setDividerLocation(0.75);
        });
    }
    private static void RobotsAppearing(){
        int corner;
        int x = 0, y = 0;
        final int DELAYMIN = 1;
        final int DELAYMAX = 4;


        while(true){
            corner = (int)(Math.random()*(DELAYMAX-DELAYMIN+1)+DELAYMIN);

            switch (corner) {
                case 1 -> {
                    x = 0;
                    y = 0;
                }
                case 2 -> {
                    x = 0;
                    y = 8;
                }
                case 3 -> {
                    x = 8;
                    y = 0;
                }
                case 4 -> {
                    x = 8;
                    y = 8;
                }
            }
            setRobotXandY(x, y);
        }
    }
    private static void setRobotXandY(int x, int y){
        boolean occupied;
        int delay;
        final int DELAYMIN = 500;
        final int DELAYMAX = 2000;
        final int OCCUPIED_ROBOTS_WALLS = 3;


        delay = (int)(Math.random()*(DELAYMAX-DELAYMIN+1)+DELAYMIN);

        if(robotNumber == 1){

            arena.setRobotPosition(""+robotNumber, new XandYObject(x, y, delay), 0);
            CompletableFuture.runAsync(() -> RandomMovingAttempt(""+robotNumber, new XandYObject(x, y, delay)), executorService);

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            robotNumber++;
        }
        else{
            occupied = IsOccupied(x, y, OCCUPIED_ROBOTS_WALLS);

            if(occupied){
                // do nothing
            }
            else{
                if(robotNumber < ROBOTS_LIMIT){
                    arena.setRobotPosition(""+robotNumber, new XandYObject(x, y, delay), 0);
                    CompletableFuture.runAsync(() -> RandomMovingAttempt(""+robotNumber, new XandYObject(x, y, delay)), executorService);

                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    robotNumber++;
                }
            }
        }


    }
    private static void TowardsTheCitadel(String robotName, XandYObject xandYObject){
        int direction;
        int delay;
        int x, y =0;
        final int DELAYMAX = 2;
        final int DELAYMIN = 1;
        boolean freeToMove;


        delay = xandYObject.getDelay();
        x = xandYObject.getNewX();
        y = xandYObject.getNewY();

        while(!xandYObject.isDestroyed()){

            // xandYObject.isDestroyed()

//            String s = String.format("%s, %s, %b", robotName, "Destroyed", destroyedRobots.containsKey(robotName));
//            System.out.println(s);

            direction = (int) (Math.random() * (DELAYMAX - DELAYMIN + 1) + DELAYMIN);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if(x < 4 && y < 4){ // Upper left corner
                switch (direction) {
                    case 1 -> { // Moves right
                        if (x < 8) {
                            x++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
//                            if (!freeToMove) {
//                                x--;
//                                RandomMovingAttempt(robotName, xandYObject);
//                            }
                        }
                    }
                    case 2 -> { // Moves down
                        if (y < 8) {
                            y++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
//                            if (!freeToMove) {
//                                y--;
//                                RandomMovingAttempt(robotName, xandYObject);
//                            }
                        }
                    }
                }
            }else if(x >= 4 && y < 4){ // Upper right corner
                switch (direction) {
                    case 1 -> { // Moves left
                        if (x > 0) {
                            x--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
//                            if (!freeToMove) {
//                                x++;
//                                RandomMovingAttempt(robotName, xandYObject);
//                            }
                        }
                    }
                    case 2 -> { // Moves down
                        if (y < 8) {
                            y++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
//                            if (!freeToMove) {
//                                y--;
//                                RandomMovingAttempt(robotName, xandYObject);
//                            }
                        }
                    }
                }
            }else if(x < 4 && y >= 4) { // Bottom left corner
                switch (direction) {
                    case 1 -> { // Moves up
                        if (y > 0) {
                            y--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
//                            if (!freeToMove) {
//                                y++;
//                                RandomMovingAttempt(robotName, xandYObject);
//                            }
                        }
                    }
                    case 2 -> { // Moves right
                        if (x < 8) {
                            x++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
//                            if (!freeToMove) {
//                                x--;
//                                RandomMovingAttempt(robotName, xandYObject);
//                            }
                        }
                    }
                }
            }else if(x >= 4 && y >= 4){ // Bottom right corner
                switch (direction) {
                    case 1 -> { // Moves up
                        if (y > 0) {
                            y--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
//                            if (!freeToMove) {
//                                y++;
//                                RandomMovingAttempt(robotName, xandYObject);
//                            }
                        }
                    }
                    case 2 -> { // Moves left
                        if (x > 0) {
                            x--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
//                            if (!freeToMove) {
//                                x++;
//                                RandomMovingAttempt(robotName, xandYObject);
//                            }
                        }
                    }
                }
            }
        }
    }
    private static void RandomMovingAttempt(String robotName, XandYObject xandYObject){
        int direction;
        int delay;
        int x, y;
        Boolean freeToMove;
        final int DELAYMAX = 4;
        final int DELAYMIN = 1;

        delay = xandYObject.getDelay();
        x = xandYObject.getNewX();
        y = xandYObject.getNewY();

        while(true){
            direction = (int) (Math.random() * (DELAYMAX - DELAYMIN + 1) + DELAYMIN);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            switch (direction) {
                case 1 -> { // Moves left
                    if (x > 0) {
                        x--;
                        freeToMove = FreeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            x++;
                        }
                    }
                }
                case 2 -> { // Moves right
                    if (x < 8) {
                        x++;
                        freeToMove = FreeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            x--;
                        }
                    }
                }
                case 3 -> { // Moves up
                    if (y > 0) {
                        y--;
                        freeToMove = FreeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            y++;
                        }
                    }
                }
                case 4 -> { // Moves down
                    if (y < 8) {
                        y++;
                        freeToMove = FreeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            y--;
                        }
                    }
                }
            }

        }
    }
    private static boolean FreeToMove(String robotName, XandYObject xandYObject, int newX, int newY){
        boolean freeToMove = true;

        if(IsOccupied(newX, newY, OCCUPIED_ROBOTS))
            freeToMove = false;

        if(IsOccupied(newX, newY, OCCUPIED_UNDAMAGED_WALLS)){
            xandYObject.setOldX(xandYObject.getNewX());
            xandYObject.setOldY(xandYObject.getNewY());
            xandYObject.setNewX(newX);
            xandYObject.setNewY(newY);
            xandYObject.setDestroyed(true);
            arena.setRobotPosition(robotName, xandYObject);
            arena.setWallPosition(newX, newY, WALL_WEAKENED);
        }

        if(IsOccupied(newX, newY, OCCUPIED_WEAKENED_WALLS)){
            xandYObject.setOldX(xandYObject.getNewX());
            xandYObject.setOldY(xandYObject.getNewY());
            xandYObject.setNewX(newX);
            xandYObject.setNewY(newY);
            xandYObject.setDestroyed(true);
            arena.setRobotPosition(robotName, xandYObject);
            arena.setWallPosition(newX, newY, NO_WALL);
            wallCount--;
        }

        if(freeToMove){
            xandYObject.setOldX(xandYObject.getNewX());
            xandYObject.setOldY(xandYObject.getNewY());
            xandYObject.setNewX(newX);
            xandYObject.setNewY(newY);
            arena.setRobotPosition(robotName, xandYObject);

        }

        return freeToMove;
    }
    private static void FortressWall(BlockingQueue<XandYObject> blockingQueue){
        boolean occupied;
        XandYObject xandYObject;
        int x, y;

        while(true){
            try {
                xandYObject = blockingQueue.take();

                x = xandYObject.getNewX();
                y = xandYObject.getNewY();

                occupied = IsOccupied(x, y, OCCUPIED_ALL);

                if(!occupied && wallCount < 10){
                    arena.setWallPosition(x, y, WALL_UNDAMAGED);
                    wallCount++;

//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }

//            String s = String.format("%s, %s", x, y);
//            logger.append(s+"\n");
                }
//                else{
//                    Map<String, XandYObject> robotsMap;
//                    robotsMap = arena.getRobotsMap();
//
//                    for(Map.Entry<String, XandYObject> e : robotsMap.entrySet()){
//                        XandYObject o = e.getValue();
//
//                        if((x == o.getNewX() && y == o.getNewY() ) || (x == o.getOldX() && y == o.getOldY())){
//                            String s = String.format("%s, %d, %d, %d, %d", e.getKey(), o.getOldX(), o.getOldY(), o.getNewX(), o.getNewY());
//                            logger.append(s+"\n");
//                        }
//                    }
//                }

            } catch (InterruptedException e) {
                System.out.println(e);
                throw new RuntimeException(e);

            }

        }
    }
    private static boolean IsOccupied(int x, int y, int occupiedBy){
        boolean occupied = false;
        Map<String, XandYObject> robotsMap;
        int wallArray[][];
        int robotArray[][];

        robotsMap = arena.getRobotsMap();
        wallArray = arena.getWallArray();
        robotArray = arena.getRobotArray();

        switch (occupiedBy) {
            case 1 -> { // Occupied by citadel
                if(x == arena.getCITADEL_X() && y == arena.getCITADEL_Y()){
                    occupied = true;
                }
            }
            case 2 -> { // Occupied by walls
                if (wallArray[x][y] == 1 || wallArray[x][y] == 2)
                    occupied = true;
            }
            case 3 -> { // Occupied by robots and walls
                for (XandYObject o : robotsMap.values()) {
                    if ((x == o.getNewX() && y == o.getNewY()) || (x == o.getOldX() && y == o.getOldY())) {
                        occupied = true;
                    }
                }
                if (wallArray[x][y] == 1)
                    occupied = true;
            }
            case 4 -> { // Occupied by all
                for (XandYObject o : robotsMap.values()) {
                    if ((x == o.getNewX() && y == o.getNewY()) || (x == o.getOldX() && y == o.getOldY())) {
                        occupied = true;
                    }
                }
                if (wallArray[x][y] == 1)
                    occupied = true;

                if(x == arena.getCITADEL_X() && y == arena.getCITADEL_Y())
                    occupied = true;
            }
            case 5 -> {
                for (XandYObject o : robotsMap.values()) {
                    if ((x == o.getNewX() && y == o.getNewY()) || (x == o.getOldX() && y == o.getOldY())) {
                        occupied = true;
                    }
                }
            }
            case 6 -> { // Occupied by weakened walls
                if (wallArray[x][y] == 2)
                    occupied = true;
            }
            case 7 -> { // Occupied by undamaged walls
                if (wallArray[x][y] == 1)
                    occupied = true;
            }
        }



//        if(robotArray[x][y] == 1)
//            occupied = true;


        return occupied;
    }

}
