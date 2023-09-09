package edu.curtin.saed.assignment1;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;
import javax.swing.*;

public class App 
{
    private static JTextArea logger;
    private static BlockingQueue<XandYObject> blockingQueue = new ArrayBlockingQueue<>(100);
    private static ExecutorService executorService;
    private static SwingArena arena;
    private static int robotNumber = 1;
    private BlockingQueue<SwingArena> queue = new ArrayBlockingQueue<>(81);
    public static void main(String[] args) 
    {
        executorService = Executors.newFixedThreadPool(81);

        // Note: SwingUtilities.invokeLater() is equivalent to JavaFX's Platform.runLater().
        SwingUtilities.invokeLater(() ->
        {
            JFrame window = new JFrame("Example App (Swing)");
            arena = new SwingArena();

            CompletableFuture.runAsync(App::RobotsAppearing, executorService);
//            CompletableFuture.runAsync(App::MoveAttempt, executorService);
//            CompletableFuture.runAsync(App::methodThree, executorService);
//            CompletableFuture.runAsync(App::methodFour, executorService);

            arena.addListener((x, y) ->
            {
//                System.out.println("Arena click at (" + x + "," + y + ")");
//                CompletableFuture.runAsync(() -> FortressWall(x, y));
                FortressWall(x, y);
            });
            
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

        delay = (int)(Math.random()*(DELAYMAX-DELAYMIN+1)+DELAYMIN);

        if(robotNumber == 1){

            arena.setRobotPosition(""+robotNumber, new XandYObject(x, y, delay), 0);
            CompletableFuture.runAsync(() -> TowardsTheCitadel(""+robotNumber, new XandYObject(x, y, delay)), executorService);

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            robotNumber++;
        }
        else{
            occupied = IsOccupied(x, y);

            if(occupied){
                // do nothing
            }
            else{
                arena.setRobotPosition(""+robotNumber, new XandYObject(x, y, delay), 0);
                CompletableFuture.runAsync(() -> TowardsTheCitadel(""+robotNumber, new XandYObject(x, y, delay)), executorService);

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                robotNumber++;

            }
        }


    }
    private static void TowardsTheCitadel(String robotName, XandYObject xandYObject){
        int direction;
        int delay;
        int x, y =0;
        final int DELAYMAX = 2;
        final int DELAYMIN = 1;
        Boolean freeToMove;


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

            if(x < 4 && y < 4){ // Upper left corner
                switch (direction) {
                    case 1 -> { // Moves right
                        if (x < 8) {
                            x++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x--;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves down
                        if (y < 8) {
                            y++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y--;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                }
            }else if(x >= 4 && y < 4){ // Upper right corner
                switch (direction) {
                    case 1 -> { // Moves left
                        if (x > 0) {
                            x--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x++;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves down
                        if (y < 8) {
                            y++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y--;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                }
            }else if(x < 4 && y >= 4) { // Bottom left corner
                switch (direction) {
                    case 1 -> { // Moves up
                        if (y > 0) {
                            y--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y++;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves right
                        if (x < 8) {
                            x++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x--;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                }
            }else if(x >= 4 && y >= 4){ // Bottom right corner
                switch (direction) {
                    case 1 -> { // Moves up
                        if (y > 0) {
                            y--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y++;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves left
                        if (x > 0) {
                            x--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x++;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
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
        Map<String, XandYObject> robotsMap;
        int[][] wallArray;
        int[][] robotArray;
        boolean freeToMove = true;

        robotsMap = arena.getRobotsMap();
        wallArray = arena.getWallArray();
        robotArray = arena.getRobotArray();

        if(IsOccupied(newX, newY)){
            freeToMove = false;
        }


//        if(robotArray[newX][newY] == 1)
//            freeToMove = false;
//
        if(wallArray[newX][newY] == 1)
            freeToMove = false;


//        System.out.println(robotArray[newX][newY]);

        if(freeToMove){

                xandYObject.setOldX(xandYObject.getNewX());
                xandYObject.setOldY(xandYObject.getNewY());
                xandYObject.setNewX(newX);
                xandYObject.setNewY(newY);
                arena.setRobotPosition(robotName, xandYObject);

            if(newX == arena.getCITADEL_X() && newY == arena.getCITADEL_Y()){
//                executorService.shutdownNow();
            }
        }

        return freeToMove;
    }
    private static void FortressWall(int x, int y){
        boolean occupied;

        occupied = IsOccupied(x, y);

        if(!occupied){
            arena.setWallPosition(x, y);

//            String s = String.format("%s, %s", x, y);
//            logger.append(s+"\n");
        }
        else{
            Map<String, XandYObject> robotsMap;
            robotsMap = arena.getRobotsMap();

            for(Map.Entry<String, XandYObject> e : robotsMap.entrySet()){
                XandYObject o = e.getValue();

                if((x == o.getNewX() && y == o.getNewY() ) || (x == o.getOldX() && y == o.getOldY())){ // (x == arena.getCITADEL_X() && y == arena.getCITADEL_Y())
                    String s = String.format("%s, %d, %d, %d, %d", e.getKey(), o.getOldX(), o.getOldY(), o.getNewX(), o.getNewY());
                    logger.append(s+"\n");

                }

            }


        }

    }
    private static boolean IsOccupied(int x, int y){
        boolean occupied = false;
        Map<String, XandYObject> robotsMap;
        int wallArray[][];
        int robotArray[][];


        robotsMap = arena.getRobotsMap();
        wallArray = arena.getWallArray();
        robotArray = arena.getRobotArray();

        for(XandYObject o :  robotsMap.values()){
            if((x == o.getNewX() && y == o.getNewY() ) || (x == o.getOldX() && y == o.getOldY())){ // (x == arena.getCITADEL_X() && y == arena.getCITADEL_Y())
                occupied = true;
            }
        }

        if(wallArray[x][y] == 1)
            occupied = true;

//        if(robotArray[x][y] == 1)
//            occupied = true;


        return occupied;
    }

}
