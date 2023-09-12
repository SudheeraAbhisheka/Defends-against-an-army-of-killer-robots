package edu.curtin.saed.assignment1;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.*;
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
    private final static int ROBOTS_LIMIT = 50; // temporary
    private static int[][] wallArray;
    private static int[][] robotArray;
    private static final JToolBar toolbar = new JToolBar();
    private static final ConcurrentHashMap<String, XandYObject> robotsMap = new ConcurrentHashMap<>();
    private static int marks = 0;
    private static JLabel jLabelScore;
    private static JLabel jLabelQueuedUpWalls;
    private static final Object mutex = new Object();
    private static final int WALLS_LIMIT = 10;
    private static boolean start;
    private static int gridWidth;
    private static int gridHeight;
    public static void main(String[] args) {
        BlockingQueue<XandYObject> blockingQueue = new ArrayBlockingQueue<>(20);
        final int OCCUPIED_CITADEL = 1;
        final int NO_DELAY = 0;

        executorService = Executors.newCachedThreadPool();

        SwingUtilities.invokeLater(() ->
        {
            JFrame window = new JFrame("Example App (Swing)");

            arena = new SwingArena();

            gridWidth = arena.getGridWidth();
            gridHeight = arena.getGridHeight();

            wallArray = new int[gridWidth][gridHeight];


            JButton btn1 = new JButton("Start");
            toolbar.add(btn1);

            jLabelScore = new JLabel("Score: "+marks);
            jLabelQueuedUpWalls = new JLabel("   Queued-up walls: "+blockingQueue.size());


            toolbar.add(jLabelScore);
            toolbar.add(jLabelQueuedUpWalls);



             btn1.addActionListener((event) ->
             {
                 arena.Start();
                 start = true;

                 CompletableFuture.runAsync(App::RobotsStartupCoordinates, executorService);
                 CompletableFuture.runAsync(App::displayingThread, executorService);

                 CompletableFuture.runAsync(() -> FortressWall(blockingQueue), executorService);
                 CompletableFuture.runAsync(App::EachSecondTenMarks, executorService);
             });

            arena.addListener((x, y) ->
            {
                if(!IsOccupied(x, y, OCCUPIED_CITADEL) && start){
                    if(wallCount <= WALLS_LIMIT) {
                        blockingQueue.add(new XandYObject(x, y, NO_DELAY));

                        jLabelQueuedUpWalls.setText("   Queued-up walls: "+ blockingQueue.size());

                    }
                }
            });

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
    private static void RobotsStartupCoordinates(){
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
                    y = gridHeight - 1;
                }
                case 3 -> {
                    x = gridWidth - 1;
                    y = 0;
                }
                case 4 -> {
                    x = gridWidth - 1;
                    y = gridHeight - 1;
                }
            }
            GenerateRobots(x, y);

        }
    }
    private static void GenerateRobots(int x, int y){
        int delay;
        final int DELAYMIN = 500;
        final int DELAYMAX = 2000;

        delay = (int)(Math.random()*(DELAYMAX-DELAYMIN+1)+DELAYMIN);

        if(robotsMap.isEmpty()){
            robotsMap.put(""+robotNumber, new XandYObject(x, y, delay));
            CompletableFuture.runAsync(() -> TowardsTheCitadel(""+robotNumber, new XandYObject(x, y, delay)), executorService);

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            robotNumber++;
        }
        else{
            if(IsOccupied(x, y, OCCUPIED_ROBOTS)){

            }
            else if(IsOccupied(x, y, OCCUPIED_WEAKENED_WALLS)){
                wallArray[x][y] = NO_WALL;
                wallCount--;

                synchronized (mutex){
                    marks = marks + 100;
                    jLabelScore.setText("Score: "+marks);
                }

                if(robotNumber < ROBOTS_LIMIT){
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    robotNumber++;
                }
            }
            else if(IsOccupied(x, y, OCCUPIED_UNDAMAGED_WALLS)){
                wallArray[x][y] = WALL_WEAKENED;

                synchronized (mutex){
                    marks = marks + 100;
                    jLabelScore.setText("Score: "+marks);
                }

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                robotNumber++;
            }
            else{
                XandYObject xandYObject = new XandYObject(x, y, delay);
                robotsMap.put(""+robotNumber, new XandYObject(x, y, delay));
                CompletableFuture.runAsync(() -> TowardsTheCitadel(""+robotNumber, xandYObject), executorService);

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
        int x, y;
        final int DELAYMAX = 2;
        final int DELAYMIN = 1;
        boolean freeToMove;


        delay = xandYObject.getDelay();
        x = xandYObject.getNewX();
        y = xandYObject.getNewY();

        while(!xandYObject.isDestroyed()){

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

        while(!xandYObject.isDestroyed()){
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

        else if(IsOccupied(newX, newY, OCCUPIED_UNDAMAGED_WALLS)){
            xandYObject.setOldX(xandYObject.getNewX());
            xandYObject.setOldY(xandYObject.getNewY());
            xandYObject.setNewX(newX);
            xandYObject.setNewY(newY);
            xandYObject.setDestroyed();

            xandYObject.startTimer();
            robotsMap.put(robotName, xandYObject);
            wallArray[newX][newY] = WALL_WEAKENED;

            synchronized (mutex){
                marks = marks + 100;
                jLabelScore.setText("Score: "+marks);
            }

            CompletableFuture.runAsync(() -> RemoveRobot(robotName), executorService);
        }

        else if(IsOccupied(newX, newY, OCCUPIED_WEAKENED_WALLS)){
            xandYObject.setOldX(xandYObject.getNewX());
            xandYObject.setOldY(xandYObject.getNewY());
            xandYObject.setNewX(newX);
            xandYObject.setNewY(newY);
            xandYObject.setDestroyed();

            xandYObject.startTimer();
            robotsMap.put(robotName, xandYObject);
            wallArray[newX][newY] = NO_WALL;
            wallCount--;

            synchronized (mutex){
                marks = marks + 100;
                jLabelScore.setText("Score: "+marks);
            }

            CompletableFuture.runAsync(() -> RemoveRobot(robotName), executorService);
        }
        else{
            xandYObject.setOldX(xandYObject.getNewX());
            xandYObject.setOldY(xandYObject.getNewY());
            xandYObject.setNewX(newX);
            xandYObject.setNewY(newY);
            xandYObject.startTimer();
            robotsMap.put(robotName, xandYObject);
        }

        return freeToMove;
    }
    private static void FortressWall(BlockingQueue<XandYObject> blockingQueue){
        XandYObject xandYObject;
        int x, y;

        while(true){
            try {
                xandYObject = blockingQueue.take();
                jLabelQueuedUpWalls.setText("   Queued-up walls: "+ blockingQueue.size());

                x = xandYObject.getNewX();
                y = xandYObject.getNewY();

                if(!IsOccupied(x, y, OCCUPIED_ALL) && wallCount < WALLS_LIMIT){
                    wallArray[x][y] = WALL_UNDAMAGED;
                    wallCount++;

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

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

        robotsMap = arena.getRobotsMap();
        wallArray = arena.getWallArray();

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
                if (wallArray[x][y] == 1 || wallArray[x][y] == 2)
                    occupied = true;
            }
            case 4 -> { // Occupied by all
                for (XandYObject o : robotsMap.values()) {
                    if ((x == o.getNewX() && y == o.getNewY()) || (x == o.getOldX() && y == o.getOldY())) {
                        occupied = true;
                    }
                }
                if (wallArray[x][y] == 1 || wallArray[x][y] == 2)
                    occupied = true;

                if(x == arena.getCITADEL_X() && y == arena.getCITADEL_Y())
                    occupied = true;
            }
            case 5 -> { // Occupied by robots
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
    private static void displayingThread(){
        while(true){
            arena.setRobotPosition(robotsMap, wallArray);

//            try {
//                Thread.sleep(20);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        }
    }
    private static void RemoveRobot(String robotName){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        robotsMap.remove(robotName);
    }
    private static void EachSecondTenMarks(){
        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            synchronized (mutex){
                marks = marks + 10;
                jLabelScore.setText("Score: "+marks);
            }

        }



    }
}
