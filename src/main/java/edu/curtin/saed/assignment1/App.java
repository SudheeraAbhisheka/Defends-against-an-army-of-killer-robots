package edu.curtin.saed.assignment1;

import java.awt.*;
import java.util.concurrent.*;
import javax.swing.*;

public class App
{
    private static JTextArea logger;
    private static ExecutorService executorService;
    private static SwingArena arena;
    private static int robotNumber = 1;
    private final static int OCCUPIED_CITADEL = 1;
    private final static int OCCUPIED_ALL = 4;
    private final static int OCCUPIED_WEAKENED_WALLS = 6;
    private final static int OCCUPIED_UNDAMAGED_WALLS = 7;
    private final static int OCCUPIED_ROBOTS = 5;
    private final static int WALL_UNDAMAGED = 1;
    private final static int WALL_WEAKENED = 2;
    private final static int NO_WALL = 0;
    private static int wallCount;
    private static int[][] wallArray;
    private static JToolBar toolbar = new JToolBar();
    private static ConcurrentHashMap<String, XandYObject> robotsMap = new ConcurrentHashMap<>();
    private static int marks = 0;
    private static JLabel jLabelScore;
    private static JLabel jLabelQueuedUpWalls;
    private static Object mutex = new Object();
    private static final int WALLS_LIMIT = 10;
    private static boolean start;
    private static int gridWidth;
    private static int gridHeight;
    private static int citadelX;
    private static int citadelY;
    public static void main(String[] args) {
        BlockingQueue<XandYObject> blockingQueue = new ArrayBlockingQueue<>(20);
        final int noDelay = 0;

        executorService = Executors.newCachedThreadPool();

        SwingUtilities.invokeLater(() ->
        {
            JFrame window = new JFrame("Example App (Swing)");

            arena = new SwingArena();

            gridWidth = arena.getGridWidth();
            gridHeight = arena.getGridHeight();

            citadelX = (int)arena.getCITADEL_X();
            citadelY = (int)arena.getCITADEL_Y();

            wallArray = new int[gridWidth][gridHeight];

            JButton btn1 = new JButton("Start");
            toolbar.add(btn1);

            jLabelScore = new JLabel("   Score: "+marks);
            jLabelQueuedUpWalls = new JLabel("   Queued-up walls: "+blockingQueue.size());


            toolbar.add(jLabelScore);
            toolbar.add(jLabelQueuedUpWalls);



             btn1.addActionListener((event) ->
             {
                 arena.start();
                 if(!start){
                     CompletableFuture.runAsync(App::robotsStartupCoordinates, executorService);
                     CompletableFuture.runAsync(() -> fortressWall(blockingQueue), executorService);
                     CompletableFuture.runAsync(App::eachSecondTenMarks, executorService);
                     CompletableFuture.runAsync(App::displayingThread, executorService);

                     start = true;
                 }

             });

            arena.addListener((x, y) ->
            {
                if(!isOccupied(x, y, OCCUPIED_CITADEL) && start){
                    if(wallCount <= WALLS_LIMIT) {
                        blockingQueue.add(new XandYObject(x, y, noDelay));

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
    private static void robotsStartupCoordinates(){
        int corner;
        int x = 0, y = 0;
        final int delayMIN = 1;
        final int delayMAX = 4;


        while(true){
            corner = (int)(Math.random()*(delayMAX-delayMIN+1)+delayMIN);

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
                default -> {
                    x = 0;
                    y = 0;
                }
            }
            generateRobots(x, y);

        }
    }
    private static void generateRobots(int x, int y){
        int delay;
        final int delayMIN = 500;
        final int delayMAX = 2000;

        delay = (int)(Math.random()*(delayMAX-delayMIN+1)+delayMIN);

        if(robotsMap.isEmpty()){
            robotsMap.put(""+robotNumber, new XandYObject(x, y, delay));
            CompletableFuture.runAsync(() -> towardsTheCitadel(""+robotNumber, new XandYObject(x, y, delay)), executorService);
            String s = String.format("Robot %s is created", robotNumber);
            logger.append(s+"\n");


            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            robotNumber++;
        }
        else{
            if(isOccupied(x, y, OCCUPIED_ROBOTS)){
                // do nothing
            }
            else if(isOccupied(x, y, OCCUPIED_WEAKENED_WALLS)){
                wallArray[x][y] = NO_WALL;
                wallCount--;

                synchronized (mutex){
                    marks = marks + 100;
                    jLabelScore.setText("   Score: "+marks);
                }

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);

                }

                robotNumber++;
            }
            else if(isOccupied(x, y, OCCUPIED_UNDAMAGED_WALLS)){
                wallArray[x][y] = WALL_WEAKENED;

                synchronized (mutex){
                    marks = marks + 100;
                    jLabelScore.setText("   Score: "+marks);
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
                CompletableFuture.runAsync(() -> towardsTheCitadel(""+robotNumber, xandYObject), executorService);

                String s = String.format("Robot %s is created", robotNumber);
                logger.append(s+"\n");

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);

                }

                robotNumber++;
            }

        }


    }
    private static void towardsTheCitadel(String robotName, XandYObject xandYObject){
        int direction;
        int delay;
        int x, y;
        final int delayMAX = 2;
        final int delayMIN = 1;
        boolean freeToMove;


        delay = xandYObject.getDelay();
        x = xandYObject.getNewX();
        y = xandYObject.getNewY();

        while(!xandYObject.isDestroyed()){

            direction = (int) (Math.random() * (delayMAX - delayMIN + 1) + delayMIN);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);

            }

            if(x < citadelX && y < citadelY){ // Upper left corner
                switch (direction) {
                    case 1 -> { // Moves right
                        if (x < gridWidth - 1) {
                            x++;
                            freeToMove = freeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x--;
                                randomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves down
                        if (y < gridHeight + 1) {
                            y++;
                            freeToMove = freeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y--;
                                randomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    default -> {// do nothing
                    }
                }
            }else if(x >= citadelX && y < citadelY){ // Upper right corner
                switch (direction) {
                    case 1 -> { // Moves left
                        if (x > 0) {
                            x--;
                            freeToMove = freeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x++;
                                randomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves down
                        if (y < gridHeight - 1) {
                            y++;
                            freeToMove = freeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y--;
                                randomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    default -> {// do nothing
                    }
                }
            }else if(x < citadelX && y >= citadelY) { // Bottom left corner
                switch (direction) {
                    case 1 -> { // Moves up
                        if (y > 0) {
                            y--;
                            freeToMove = freeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y++;
                                randomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves right
                        if (x < gridWidth) {
                            x++;
                            freeToMove = freeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x--;
                                randomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    default -> {// do nothing
                    }
                }
            }else if(x >= citadelX && y >= citadelY){ // Bottom right corner
                switch (direction) {
                    case 1 -> { // Moves up
                        if (y > 0) {
                            y--;
                            freeToMove = freeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y++;
                                randomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves left
                        if (x > 0) {
                            x--;
                            freeToMove = freeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x++;
                                randomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    default -> {// do nothing
                    }
                }
            }
        }
    }
    private static void randomMovingAttempt(String robotName, XandYObject xandYObject){
        int direction;
        int delay;
        int x, y;
        boolean freeToMove;
        final int delayMAX = 4;
        final int delayMIN = 1;

        delay = xandYObject.getDelay();
        x = xandYObject.getNewX();
        y = xandYObject.getNewY();

        while(!xandYObject.isDestroyed()){
            direction = (int) (Math.random() * (delayMAX - delayMIN + 1) + delayMIN);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);

            }


            switch (direction) {
                case 1 -> { // Moves left
                    if (x > 0) {
                        x--;
                        freeToMove = freeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            x++;
                        }
                    }
                }
                case 2 -> { // Moves right
                    if (x < gridWidth - 1) {
                        x++;
                        freeToMove = freeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            x--;
                        }
                    }
                }
                case 3 -> { // Moves up
                    if (y > 0) {
                        y--;
                        freeToMove = freeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            y++;
                        }
                    }
                }
                case 4 -> { // Moves down
                    if (y < gridHeight - 1) {
                        y++;
                        freeToMove = freeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            y--;
                        }
                    }
                }
                default -> {// do nothing
                }
            }

        }
    }
    private static boolean freeToMove(String robotName, XandYObject xandYObject, int newX, int newY){
        boolean freeToMove = true;

        if(isOccupied(newX, newY, OCCUPIED_CITADEL)){
            String s = String.format("%s", "Gave over!");
            logger.append(s+"\n");

            executorService.shutdownNow();
        }

        if(isOccupied(newX, newY, OCCUPIED_ROBOTS)){
            freeToMove = false;
        }

        else if(isOccupied(newX, newY, OCCUPIED_UNDAMAGED_WALLS)){
            xandYObject.setOldX(xandYObject.getNewX());
            xandYObject.setOldY(xandYObject.getNewY());
            xandYObject.setNewX(newX);
            xandYObject.setNewY(newY);
            xandYObject.setDestroyed();

            xandYObject.startTimer();
            robotsMap.put(robotName, xandYObject);
            wallArray[newX][newY] = WALL_WEAKENED;

            String s = String.format("A wall got weakened.");
            logger.append(s+"\n");

            synchronized (mutex){
                marks = marks + 100;
                jLabelScore.setText("   Score: "+marks);
            }

            CompletableFuture.runAsync(() -> removeRobot(robotName), executorService);
        }

        else if(isOccupied(newX, newY, OCCUPIED_WEAKENED_WALLS)){
            xandYObject.setOldX(xandYObject.getNewX());
            xandYObject.setOldY(xandYObject.getNewY());
            xandYObject.setNewX(newX);
            xandYObject.setNewY(newY);
            xandYObject.setDestroyed();

            xandYObject.startTimer();
            robotsMap.put(robotName, xandYObject);
            wallArray[newX][newY] = NO_WALL;
            wallCount--;

            String s = String.format("A wall got destroyed. Remaining wall count - %s", wallCount);
            logger.append(s+"\n");

            synchronized (mutex){
                marks = marks + 100;
                jLabelScore.setText("   Score: "+marks);
            }

            CompletableFuture.runAsync(() -> removeRobot(robotName), executorService);
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
    private static void fortressWall(BlockingQueue<XandYObject> blockingQueue){
        XandYObject xandYObject;
        int x, y;

        while(true){
            try {
                xandYObject = blockingQueue.take();
                jLabelQueuedUpWalls.setText("   Queued-up walls: "+ blockingQueue.size());

                x = xandYObject.getNewX();
                y = xandYObject.getNewY();

                if(!isOccupied(x, y, OCCUPIED_ALL) && wallCount < WALLS_LIMIT){
                    wallArray[x][y] = WALL_UNDAMAGED;
                    wallCount++;

                    String s = String.format("Wall %s is created", wallCount);
                    logger.append(s+"\n");

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
    private static boolean isOccupied(int x, int y, int occupiedBy){
        boolean occupied = false;

        switch (occupiedBy) {
            case 1 -> { // Occupied by citadel
                if(x == citadelX && y == citadelY){
                    occupied = true;
                }
            }
            case 2 -> { // Occupied by walls
                if (wallArray[x][y] == 1 || wallArray[x][y] == 2){
                    occupied = true;
                }
            }
            case 3 -> { // Occupied by robots and walls
                for (XandYObject o : robotsMap.values()) {
                    if ((x == o.getNewX() && y == o.getNewY()) || (x == o.getOldX() && y == o.getOldY())) {
                        occupied = true;
                    }
                }

                if (wallArray[x][y] == 1 || wallArray[x][y] == 2){
                    occupied = true;
                }
            }
            case 4 -> { // Occupied by all
                for (XandYObject o : robotsMap.values()) {
                    if ((x == o.getNewX() && y == o.getNewY()) || (x == o.getOldX() && y == o.getOldY())) {
                        occupied = true;
                    }
                }
                if (wallArray[x][y] == 1 || wallArray[x][y] == 2){
                    occupied = true;
                }

                if(x == citadelX && y == citadelY){
                    occupied = true;
                }
            }
            case 5 -> { // Occupied by robots
                for (XandYObject o : robotsMap.values()) {
                    if ((x == o.getNewX() && y == o.getNewY()) || (x == o.getOldX() && y == o.getOldY())) {
                        occupied = true;
                    }
                }
            }
            case 6 -> { // Occupied by weakened walls
                if (wallArray[x][y] == 2){
                    occupied = true;
                }
            }
            case 7 -> { // Occupied by undamaged walls
                if (wallArray[x][y] == 1){
                    occupied = true;
                }
            }
            default -> { // do nothing
            }
        }

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
    private static void removeRobot(String robotName){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }
        robotsMap.remove(robotName);
    }
    private static void eachSecondTenMarks(){
        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);

            }

            synchronized (mutex){
                marks = marks + 10;
                jLabelScore.setText("   Score: "+marks);
            }

        }



    }
}
